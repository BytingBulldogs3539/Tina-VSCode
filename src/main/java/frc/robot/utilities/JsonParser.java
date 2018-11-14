/**
 * Created by Cameron Coesens in the 2019 Off Season.
 * This allows us to grab a json file from the roborio and read the 2d array
 * to give the information to the motionprofile handler
 */

package frc.robot.utilities;

import java.io.File;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;

import java.io.FileReader;

public final class JsonParser {

	public static double[][] RetrieveProfileData(File target) {

		double[][] data = null;
		String jsonData = readFile(target);
		JSONObject jobj;
		try {
			jobj = new JSONObject(jsonData);
			JSONArray jsonArray = new JSONArray(jobj.get("Data").toString());
			data = new double[jsonArray.length()][4];

			for (int i = 0; i < jsonArray.length(); i++) {
				try {

					JSONObject object1 = jsonArray.getJSONObject(i);

					double[] array = new double[4];

					array[0] = object1.getDouble("Rotation");
					array[1] = object1.getDouble("Velocity");
					array[2] = object1.getDouble("Time");
					array[3] = object1.getDouble("Angle");

					data[i] = array;

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return data;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String readFile(File filename) {
		String result = "";
		try {
			BufferedReader br = new BufferedReader(new FileReader(filename));
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();
			while (line != null) {
				sb.append(line);
				line = br.readLine();
			}
			br.close();
			result = sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}
}
