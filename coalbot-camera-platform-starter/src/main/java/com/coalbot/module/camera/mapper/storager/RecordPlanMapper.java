package com.coalbot.module.camera.mapper.storager;

import com.coalbot.module.camera.service.bean.RecordPlan;
import com.coalbot.module.camera.service.bean.RecordPlanItem;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface RecordPlanMapper {

    @Insert(" <script>" +
            "INSERT INTO wcp_record_plan (" +
            " name," +
            " snap," +
            " create_time," +
            " update_time) " +
            "VALUES (" +
            " #{name}," +
            " #{snap}," +
            " #{createTime}," +
            " #{updateTime})" +
            " </script>")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    void add(RecordPlan plan);

    @Insert(" <script>" +
            "INSERT INTO wcp_record_plan_item (" +
            "start," +
            "stop, " +
            "week_day," +
            "plan_id) " +
            "VALUES" +
            "<foreach collection='planItemList' index='index' item='item' separator=','> " +
            "(#{item.start}, #{item.stop}, #{item.weekDay},#{planId})" +
            "</foreach> " +
            " </script>")
    void batchAddItem(@Param("planId") String planId, @Param("planItemList") List<RecordPlanItem> planItemList);

    @Select("select * from wcp_record_plan where  id = #{planId}")
    RecordPlan get(@Param("planId") String planId);

    @Select(" <script>" +
            " SELECT wrp.*, (select count(1) from wcp_device_channel where record_plan_id = wrp.id) AS channelCount\n" +
            " FROM wcp_record_plan wrp where  1=1" +
            " <if test='query != null'> AND (name LIKE concat('%',#{query},'%') escape '/' )</if> " +
            " </script>")
    List<RecordPlan> query(@Param("query") String query);

    @Update("UPDATE wcp_record_plan SET update_time=#{updateTime}, name=#{name}, snap=#{snap} WHERE id=#{id}")
    void update(RecordPlan plan);

    @Delete("DELETE FROM wcp_record_plan WHERE id=#{planId}")
    void delete(@Param("planId") String planId);

    @Select("select * from wcp_record_plan_item where  plan_id = #{planId}")
    List<RecordPlanItem> getItemList(@Param("planId") String planId);

    @Delete("DELETE FROM wcp_record_plan_item WHERE plan_id = #{planId}")
    void cleanItems(@Param("planId") String planId);

    @Select(" <script>" +
            " select wdc.id from wcp_device_channel wdc left join wcp_record_plan_item wrpi on wrpi.plan_id = wdc.record_plan_id " +
            " where  wrpi.week_day = #{week} and wrpi.start &lt;= #{index} and stop &gt;= #{index} group by wdc.id" +
            " </script>")
    List<String> queryRecordIng(@Param("week") int week, @Param("index") int index);
}
