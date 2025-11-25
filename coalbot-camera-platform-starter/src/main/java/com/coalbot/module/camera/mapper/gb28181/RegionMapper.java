package com.coalbot.module.camera.mapper.gb28181;

import com.coalbot.module.camera.common.CivilCodePo;
import com.coalbot.module.camera.gb28181.bean.CommonGBChannel;
import com.coalbot.module.camera.gb28181.bean.Region;
import com.coalbot.module.camera.gb28181.bean.RegionTree;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Set;

@Mapper
public interface RegionMapper {

    @Insert("INSERT INTO wcp_common_region (device_id, name, parent_id, parent_device_id, create_time, update_time) " +
            "VALUES (#{deviceId}, #{name}, #{parentId}, #{parentDeviceId}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    void add(Region region);

    @Delete("DELETE FROM wcp_common_region WHERE id=#{id}")
    int delete(@Param("id") String id);

    @Update(" UPDATE wcp_common_region " +
            " SET update_time=#{updateTime}, device_id=#{deviceId}, name=#{name}, parent_id=#{parentId}, parent_device_id=#{parentDeviceId}" +
            " WHERE id = #{id}")
    int update(Region region);

    @Select(value = {" <script>" +
            "SELECT *  from wcp_common_region WHERE 1=1 " +
            " <if test='query != null'> AND (device_id LIKE concat('%',#{query},'%') escape '/' OR name LIKE concat('%',#{query},'%') escape '/')</if> " +
            " <if test='parentId != null'> AND parent_device_id = #{parentId}</if> " +
            "ORDER BY id " +
            " </script>"})
    List<Region> query(@Param("query") String query, @Param("parentId") String parentId);

    @Select("SELECT * from wcp_common_region WHERE parent_id = #{parentId} ORDER BY id ")
    List<Region> getChildren(@Param("parentId") String parentId);

    @Select("SELECT * from wcp_common_region WHERE id = #{id} ")
    Region queryOne(@Param("id") String id);

    @Select(" select dc.civil_code as civil_code " +
            " from wcp_device_channel dc " +
            " where dc.civil_code not in " +
            " (select device_id from wcp_common_region)")
    List<String> getUninitializedCivilCode();

    @Select(" <script>" +
            " SELECT device_id from wcp_common_region " +
            " where device_id in " +
            " <foreach collection='codes'  item='item'  open='(' separator=',' close=')' > #{item}</foreach>" +
            " </script>")
    List<String> queryInList(@Param("codes") Set<String> codes);


    @Insert(" <script>" +
            " INSERT INTO wcp_common_region (" +
            " device_id," +
            " name, " +
            " parent_device_id," +
            " parent_id," +
            " create_time," +
            " update_time) " +
            " VALUES " +
            " <foreach collection='regionList' index='index' item='item' separator=','> " +
            " (#{item.deviceId}, #{item.name}, #{item.parentDeviceId},#{item.parentId},#{item.createTime},#{item.updateTime})" +
            " </foreach> " +
            " </script>")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int batchAdd(@Param("regionList") List<Region> regionList);

    @Select(" <script>" +
            " SELECT " +
            " *, " +
            " concat('region', id) as tree_id," +
            " 0 as type," +
            " 'ON' as status," +
            " false as is_leaf" +
            " from wcp_common_region " +
            " where " +
            " <if test='parentId != null'> parent_id = #{parentId} </if> " +
            " <if test='parentId == null'> parent_id is null </if> " +
            " </script>")
    List<RegionTree> queryForTree(@Param("parentId") String parentId);

    @Delete("<script>" +
            " DELETE FROM wcp_common_region WHERE id in " +
            " <foreach collection='allChildren'  item='item'  open='(' separator=',' close=')' > #{item.id}</foreach>" +
            " </script>")
    void batchDelete(@Param("allChildren") List<Region> allChildren);

    @Select(" <script>" +
            " SELECT * from wcp_common_region " +
            " where device_id in " +
            " <foreach collection='regionList'  item='item'  open='(' separator=',' close=')' > #{item.deviceId}</foreach>" +
            " </script>")
    List<Region> queryInRegionListByDeviceId(@Param("regionList") List<Region> regionList);

    @Select(" <script>" +
            " SELECT " +
            " wcr.device_id as gb_device_id," +
            " wcr.name as gb_name" +
            " from wcp_common_region wcr" +
            " left join wcp_platform_region wpr on wcr.id = wpr.region_id" +
            " where wpr.platform_id  = #{platformId} " +
            " </script>")
    List<CommonGBChannel> queryByPlatform(@Param("platformId") String platformId);

    @Update(value = " <script>" +
            " update wcp_common_region w1\n" +
            " set parent_id = w2.id\n" +
            " from wcp_common_region w2\n" +
            " where w1.parent_device_id = w2.device_id\n" +
            "  and w1.id in " +
            " <foreach collection='regionListForAdd'  item='item'  open='(' separator=',' close=')' > #{item.id}</foreach>" +
            " </script>")
    void updateParentId(@Param("regionListForAdd") List<Region> regionListForAdd);

    @Update(" <script>" +
            " update wcp_common_region" +
            " set parent_device_id = #{parentDeviceId}" +
            " where parent_id = #{parentId} " +
            " </script>")
    void updateChild(@Param("parentId") String parentId, @Param("parentDeviceId") String parentDeviceId);

    @Select("SELECT * from wcp_common_region WHERE device_id = #{deviceId} ")
    Region queryByDeviceId(@Param("deviceId") String deviceId);

    @Select(" <script>" +
            " SELECT " +
            " * " +
            " from wcp_common_region " +
            " where id in " +
            " <foreach collection='regionSet'  item='item'  open='(' separator=',' close=')' > #{item.parentId}</foreach>" +
            " </script>")
    Set<Region> queryParentInChannelList(@Param("regionSet") Set<Region> regionSet);

    @Select(" <script>" +
            " SELECT " +
            " * " +
            " from wcp_common_region " +
            " where device_id in " +
            " <foreach collection='channelList'  item='item'  open='(' separator=',' close=')' > #{item.gbCivilCode}</foreach>" +
            " order by id " +
            "</script>")
    Set<Region> queryByChannelList(@Param("channelList") List<CommonGBChannel> channelList);

    @Select(" <script>" +
            " SELECT * " +
            " from wcp_common_region wcr" +
            " left join wcp_platform_region wpr on wpr.region_id = wcr.id and wpr.platform_id = #{platformId}" +
            " where wpr.platform_id is null and wcr.device_id in " +
            " <foreach collection='channelList'  item='item'  open='(' separator=',' close=')' > #{item.gbCivilCode}</foreach>" +
            " </script>")
    Set<Region> queryNotShareRegionForPlatformByChannelList(@Param("channelList") List<CommonGBChannel> channelList, @Param("platformId") String platformId);

    @Select(" <script>" +
            " SELECT * " +
            " from wcp_common_region wcr" +
            " left join wcp_platform_region wpr on wpr.region_id = wcr.id and wpr.platform_id = #{platformId}" +
            " where wpr.platform_id IS NULL and wcr.id in " +
            " <foreach collection='allRegion'  item='item'  open='(' separator=',' close=')' > #{item.id}</foreach>" +
            " </script>")
    Set<Region> queryNotShareRegionForPlatformByRegionList(@Param("allRegion") Set<Region> allRegion, @Param("platformId") String platformId);


    @Select(" <script>" +
            " SELECT device_id " +
            " from wcp_common_region" +
            " where device_id in " +
            " <foreach collection='civilCodePoList'  item='item'  open='(' separator=',' close=')' > #{item.code}</foreach>" +
            " </script>")
    Set<String> queryInCivilCodePoList(@Param("civilCodePoList") List<CivilCodePo> civilCodePoList);
}
