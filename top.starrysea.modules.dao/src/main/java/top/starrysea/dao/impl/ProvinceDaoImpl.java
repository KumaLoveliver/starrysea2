package top.starrysea.dao.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import top.starrysea.dao.IProvinceDao;
import top.starrysea.kql.clause.WhereType;
import top.starrysea.kql.facede.EntitySqlResult;
import top.starrysea.kql.facede.KumaSqlDao;
import top.starrysea.kql.facede.ListSqlResult;
import top.starrysea.object.dto.Area;
import top.starrysea.object.dto.City;
import top.starrysea.object.dto.Province;

@Repository("areaDao")
public class ProvinceDaoImpl implements IProvinceDao {

	@Autowired
	private KumaSqlDao kumaSqlDao;

	public List<Area> getAllProvinceDao() {
		kumaSqlDao.selectMode();
		ListSqlResult<Area> theResult = kumaSqlDao.select("province_name", "p").select("province_id", "c")
				.select("city_name", "c").select("city_id", "a").select("area_id", "a").select("area_name", "a")
				.from(Province.class, "p").leftjoin(City.class, "c", "province_id", Province.class,
						"province_id")
				.leftjoin(Area.class, "a", "city_id", City.class,
						"city_id")
				.endForList((rs, row) -> new Area.Builder().areaId(rs.getInt("area_id"))
						.areaName(rs.getString("area_name"))
						.city(new City.Builder().cityId(rs.getInt("city_id")).cityName(rs.getString("city_name"))
								.province(new Province(rs.getInt("province_id"), rs.getString("province_name")))
								.build())
						.build());
		return theResult.getResult();
	}

	@Override
	public Province getProvinceByArea(int areaId) {
		kumaSqlDao.selectMode();
		EntitySqlResult<Province> result = kumaSqlDao.select("province_id", "p").from(Province.class, "p")
				.leftjoin(City.class, "c", "province_id", Province.class, "province_id")
				.leftjoin(Area.class, "a", "city_id", City.class, "city_id")
				.where("area_id", "a", WhereType.EQUALS, areaId)
				.endForObject((rs, row) -> new Province(rs.getInt("province_id")));
		return result.getResult();
	}
}
