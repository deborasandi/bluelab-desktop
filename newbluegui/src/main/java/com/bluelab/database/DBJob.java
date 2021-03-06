package com.bluelab.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.bluelab.job.Job;
import com.bluelab.jobprice.JobPrice;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class DBJob extends DBConnection {

	private static ObservableList<Job> list;
	private static Map<Integer, Job> map;

	private static Consumer<Job> callback = client -> {
	};

	private static void setCallback(Consumer<Job> c) {
		callback = c;
	}

	private static void updateData() {
		String query = "SELECT * FROM job j";

		try {
			Statement st = connection.createStatement();
			ResultSet rs = st.executeQuery(query);

			list.clear();
			map.clear();

			while (rs.next()) {
				Job j = new Job();

				j.setId(rs.getInt("id"));
				j.setClient(DBClient.get(rs.getInt("client_id")));
				j.setJobPrice(DBJobPrice.get(rs.getInt("jobprice_id")));
				j.setProductColor(DBProductColor.get(rs.getInt("productcolor_id")));
				j.setQtd(rs.getInt("qtd"));
				j.setShipping(rs.getDouble("shipping"));
				j.setDate(rs.getDate("date", Calendar.getInstance()));
				j.setRepetition(rs.getBoolean("isrepetition"));
				j.setNocost(rs.getBoolean("isnocost"));
				j.setPaid(rs.getBoolean("ispaid"));
				j.setTotal(rs.getDouble("total"));
				j.setTotalPaid(rs.getDouble("totalpaid"));
				j.setRepValue(rs.getDouble("repvalue"));
				j.setObs(rs.getString("obs"));

				list.add(j);

				map.put(j.getId(), j);
			}

			st.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void init() {
		map = new HashMap<Integer, Job>();
		list = FXCollections.observableArrayList();

		updateData();

		setCallback(c -> Platform.runLater(() -> updateData()));
	}

	public static void insert(Job j) {
		String query = "INSERT INTO job (client_id, job_price_id, color_id, qtd, shipping, date, isrepetition, isnocost, ispaid, total, totalpaid, repvalue, obs) VALUES"
				+ "(?, ?, ?, ?, ?, ?, ?, ?);";

		try {
			PreparedStatement stmt = connection.prepareStatement(query);

			stmt.setInt(1, j.getClient().getId());
			stmt.setInt(2, j.getJobPrice().getId());
			stmt.setInt(3, j.getProductColor().getId());
			stmt.setInt(4, j.getQtd());
			stmt.setDouble(5, j.getShipping());
			stmt.setDate(6, j.getDate());
			stmt.setBoolean(7, j.isRepetition());
			stmt.setBoolean(8, j.isNocost());
			stmt.setBoolean(9, j.isPaid());
			stmt.setDouble(10, j.getTotal());
			stmt.setDouble(11, j.getTotalPaid());
			stmt.setDouble(12, j.getRepValue());
			stmt.setString(13, j.getObs());

			stmt.execute();
			stmt.close();

			callback.accept(new Job());
		} catch (SQLException u) {
			throw new RuntimeException(u);
		}
	}

	public static void update(Job j) {
		String query = "UPDATE job SET client_id = ?, job_price_id = ?, color_id = ?, qtd = ?, shipping = ?, date = ?, isrepetition = ?, isnocost = ?, ispaid = ?, total = ?, totalpaid = ?, repvalue = ?, obs = ? WHERE id = ?";

		try {
			PreparedStatement stmt = connection.prepareStatement(query);

			stmt.setInt(1, j.getClient().getId());
			stmt.setInt(2, j.getJobPrice().getId());
			stmt.setInt(3, j.getProductColor().getId());
			stmt.setInt(4, j.getQtd());
			stmt.setDouble(5, j.getShipping());
			stmt.setDate(6, j.getDate());
			stmt.setBoolean(7, j.isRepetition());
			stmt.setBoolean(8, j.isNocost());
			stmt.setBoolean(9, j.isPaid());
			stmt.setDouble(10, j.getTotal());
			stmt.setDouble(11, j.getTotalPaid());
			stmt.setDouble(12, j.getRepValue());
			stmt.setString(13, j.getObs());

			stmt.execute();
			stmt.close();

			callback.accept(new Job());
		} catch (SQLException u) {
			throw new RuntimeException(u);
		}
	}

	public static void update(int id, boolean paid) {
		String query = "UPDATE job SET ispaid = ? WHERE id = ?";

		try {
			PreparedStatement stmt = connection.prepareStatement(query);

			stmt.setBoolean(1, paid);
			stmt.setInt(2, id);

			stmt.execute();
			stmt.close();

			callback.accept(new Job());
		} catch (SQLException u) {
			throw new RuntimeException(u);
		}
	}

	public static void delete(int id) {
		String query = "DELETE FROM job WHERE id = ?;";

		try {
			PreparedStatement stmt = connection.prepareStatement(query);

			stmt.setInt(1, id);

			stmt.execute();
			stmt.close();

			callback.accept(new Job());
		} catch (SQLException u) {
			throw new RuntimeException(u);
		}
	}

	public static List<JobPrice> list(int id) {
		List<JobPrice> list = new ArrayList<JobPrice>();

		DBJobPrice.getList().forEach(j -> {
			if (j.getPriceTable().getId() == id)
				list.add(j);
		});

		return list;
	}

	public static ObservableList<Job> getList() {
		return list;
	}

	public static Job get(int id) {
		return map.get(id);
	}
}
