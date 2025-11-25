package com.coalbot.module.camera.mapper.streamPush;

import com.coalbot.module.camera.common.enums.ChannelDataType;
import com.coalbot.module.camera.service.bean.StreamPushItemFromRedis;
import com.coalbot.module.camera.streamPush.bean.StreamPush;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Mapper
@Repository
public interface StreamPushMapper {

    Integer dataType = ChannelDataType.GB28181;

    @Insert("INSERT INTO wcp_stream_push (app, stream, media_server_id, server_id, push_time,  update_time, create_time, pushing, start_offline_push) VALUES" +
            "(#{app}, #{stream}, #{mediaServerId} , #{serverId} , #{pushTime} ,#{updateTime}, #{createTime}, #{pushing}, #{startOfflinePush})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int add(StreamPush streamPushItem);


    @Update(value = {" <script>" +
            "UPDATE wcp_stream_push " +
            "SET update_time=#{updateTime}" +
            "<if test=\"app != null\">, app=#{app}</if>" +
            "<if test=\"stream != null\">, stream=#{stream}</if>" +
            "<if test=\"mediaServerId != null\">, media_server_id=#{mediaServerId}</if>" +
            "<if test=\"serverId != null\">, server_id=#{serverId}</if>" +
            "<if test=\"pushTime != null\">, push_time=#{pushTime}</if>" +
            "<if test=\"pushing != null\">, pushing=#{pushing}</if>" +
            "<if test=\"startOfflinePush != null\">, start_offline_push=#{startOfflinePush}</if>" +
            "WHERE id = #{id}" +
            " </script>"})
    int update(StreamPush streamPushItem);

    @Delete("DELETE FROM wcp_stream_push WHERE id=#{id}")
    int del(@Param("id") String id);

    @Select(value = {" <script>" +
            " SELECT " +
            " st.*, " +
            " st.id as data_device_id, " +
            " wdc.*, " +
            " wdc.id as gb_id" +
            " from " +
            " wcp_stream_push st " +
            " LEFT join wcp_device_channel wdc " +
            " on wdc.data_type = 2 and st.id = wdc.data_device_id " +
            " WHERE " +
            " 1=1 " +
            " <if test='query != null'> AND (st.app LIKE concat('%',#{query},'%') escape '/' OR st.stream LIKE concat('%',#{query},'%') escape '/' " +
            " OR wdc.gb_device_id LIKE concat('%',#{query},'%') escape '/' OR wdc.gb_name LIKE concat('%',#{query},'%') escape '/')</if> " +
            " <if test='pushing == true' > AND st.pushing=true</if>" +
            " <if test='pushing == false' > AND st.pushing=false </if>" +
            " <if test='mediaServerId != null' > AND st.media_server_id=#{mediaServerId} </if>" +
            " order by st.pushing desc, st.create_time desc" +
            " </script>"})
    List<StreamPush> selectAll(@Param("query") String query, @Param("pushing") Boolean pushing, @Param("mediaServerId") String mediaServerId);

    @Select("SELECT st.*, st.id as data_device_id, wdc.*, wdc.id as gb_id FROM wcp_stream_push st LEFT join wcp_device_channel wdc on  wdc.data_type = 2 and st.id = wdc.data_device_id WHERE st.app=#{app} AND st.stream=#{stream}")
    StreamPush selectByAppAndStream(@Param("app") String app, @Param("stream") String stream);

    @Insert("<script>" +
            "Insert INTO wcp_stream_push ( " +
            " app, stream, media_server_id, server_id, push_time,  update_time, create_time, pushing, start_offline_push) " +
            " VALUES <foreach collection='streamPushItems' item='item' index='index' separator=','>" +
            " ( #{item.app}, #{item.stream}, #{item.mediaServerId},#{item.serverId} ,#{item.pushTime}, #{item.updateTime}, #{item.createTime}, #{item.pushing}, #{item.startOfflinePush} )" +
            " </foreach>" +
            " </script>")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int addAll(@Param("streamPushItems") List<StreamPush> streamPushItems);

    @Select("SELECT st.*, st.id as data_device_id, wdc.*, wdc.id as gb_id FROM wcp_stream_push st LEFT join wcp_device_channel wdc on wdc.data_type = 2 and st.id = wdc.data_device_id WHERE st.media_server_id=#{mediaServerId}")
    List<StreamPush> selectAllByMediaServerId(String mediaServerId);

    @Select("SELECT st.*, st.id as data_device_id, wdc.*, wdc.id as gb_id FROM wcp_stream_push st LEFT join wcp_device_channel wdc on wdc.data_type = 2 and st.id = wdc.data_device_id WHERE st.media_server_id=#{mediaServerId} and wdc.gb_device_id is null")
    List<StreamPush> selectAllByMediaServerIdWithOutGbID(String mediaServerId);

    @Update("UPDATE wcp_stream_push " +
            "SET pushing=#{pushing}, server_id=#{serverId}, media_server_id=#{mediaServerId} " +
            "WHERE id=#{id}")
    int updatePushStatus(StreamPush streamPush);

    @Select("<script> " +
            "SELECT st.*, st.id as data_device_id, wdc.*, wdc.id as gb_id FROM wcp_stream_push st LEFT join wcp_device_channel wdc on wdc.data_type = 2 and st.id = wdc.data_device_id " +
            "where (st.app, st.stream) in (" +
            "<foreach collection='offlineStreams' item='item' separator=','>" +
            "(#{item.app}, #{item.stream}) " +
            "</foreach>" +
            ")</script>")
    List<StreamPush> getListInList(@Param("offlineStreams") List<StreamPushItemFromRedis> offlineStreams);


    @Select("SELECT CONCAT(app,stream) from wcp_stream_push")
    List<String> getAllAppAndStream();

    @Select("select count(1) from wcp_stream_push ")
    int getAllCount();

    @Select(value = {" <script>" +
            " select count(1) from wcp_stream_push where pushing = true" +
            " </script>"})
    int getAllPushing(Boolean usePushingAsStatus);

    @MapKey("uniqueKey")
    @Select("SELECT CONCAT(wsp.app, wsp.stream) as unique_key, wsp.*, wdc.* , " +
            " wdc.id as gb_id " +
            " from wcp_stream_push wsp " +
            " LEFT join wcp_device_channel wdc on wdc.data_type = 2 and wsp.id = wdc.data_device_id")
    Map<String, StreamPush> getAllAppAndStreamMap();


    @MapKey("gbDeviceId")
    @Select("SELECT wdc.gb_device_id, wsp.id as data_device_id, wsp.*, wsp.* , wdc.id as gb_id " +
            " from wcp_stream_push wsp " +
            " LEFT join wcp_device_channel wdc on wdc.data_type = 2 and wsp.id = wdc.data_device_id")
    Map<String, StreamPush> getAllGBId();

    @Select("SELECT st.*, st.id as data_device_id, wdc.*, wdc.id as gb_id FROM wcp_stream_push st LEFT join wcp_device_channel wdc on wdc.data_type = 2 and st.id = wdc.data_device_id WHERE st.id=#{id}")
    StreamPush queryOne(@Param("id") String id);

    @Select("<script> " +
            "SELECT st.*, st.id as data_device_id, wdc.*, wdc.id as gb_id FROM wcp_stream_push st LEFT join wcp_device_channel wdc on wdc.data_type = 2 and st.id = wdc.data_device_id " +
            " where st.id in (" +
            " <foreach collection='ids' item='item' separator=','>" +
            " #{item} " +
            " </foreach>" +
            " )</script>")
    List<StreamPush> selectInSet(@Param("ids") Set<String> ids);

    @Delete("<script> " +
            "DELETE FROM wcp_stream_push WHERE" +
            " id in (" +
            "<foreach collection='streamPushList' item='item' separator=','>" +
            " #{item.id} " +
            "</foreach>" +
            ")</script>")
    void batchDel(@Param("streamPushList") List<StreamPush> streamPushList);


    @Update({"<script>" +
            "<foreach collection='streamPushItemForUpdate' item='item' separator=';'>" +
            " UPDATE" +
            " wcp_stream_push" +
            " SET update_time=#{item.updateTime}" +
            ", app=#{item.app}" +
            ", stream=#{item.stream}" +
            ", media_server_id=#{item.mediaServerId}" +
            ", server_id=#{item.serverId}" +
            ", push_time=#{item.pushTime}" +
            ", pushing=#{item.pushing}" +
            ", start_offline_push=#{item.startOfflinePush}" +
            " WHERE id=#{item.id}" +
            "</foreach>" +
            "</script>"})
    int batchUpdate(@Param("streamPushItemForUpdate") List<StreamPush> streamPushItemForUpdate);

    @Delete(" DELETE FROM wcp_stream_push" +
            " WHERE server_id = #{serverId}" +
            "  AND NOT EXISTS (" +
            "    SELECT 1 " +
            "    FROM wcp_device_channel wdc " +
            "    WHERE wdc.data_type = 2 " +
            "      AND wcp_stream_push.id = wdc.data_device_id" +
            "  );")
    void deleteWithoutGBId(@Param("serverId") String serverId);
}
