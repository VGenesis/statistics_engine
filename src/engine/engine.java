// Made by Veljko Grujic

package engine;

import java.util.ArrayList;
import java.util.Scanner;

public class engine {
	
	private static class DiscreetData{
		private float value;
		private int repetition;
		
		
		public DiscreetData(float value) {
			this.value = value;
			this.repetition = 1;
		}
		
		public void repeat() {this.repetition++;}
		
	}

	private static class ContinualData{
		private float[] range = new float[2];
		private int samples;
		
		public ContinualData(float[] range, int samples) {
			this.range[0] = range[0];
			this.range[1] = range[1];
			this.samples = samples;
		}
		
	}
	
	private static class DiscreetDataTable{
		private int count;
		private ArrayList<Float> values;
		private ArrayList<DiscreetData> sorted_values = null;
		
		public DiscreetDataTable(ArrayList<Float> values) {
			this.count = values.size();
			this.values = values;
			sort();
		}
		
		public int indexOf(float value) {
			for(int i = 0; i < sorted_values.size(); i ++) {
				if(sorted_values.get(i).value == value) {
					return i;
				}
			}
			return -1;
		}
		
		public void sort() {
			for(int i = 0; i < this.count - 1; i++) {
				for(int j = i + 1; j < this.count; j++) {
					if(values.get(j) < values.get(i)) {
						float t = values.get(j);
						values.set(j, values.get(i));
						values.set(i, t);
					}
				}
			}
			sorted_values = new ArrayList<DiscreetData>();
			for(float e : values) {
				DiscreetData val = new DiscreetData(e);
				if(sorted_values.isEmpty()) sorted_values.add(val);
				else {
					int index = indexOf(val.value);
					if(index == -1) sorted_values.add(val);
					else sorted_values.get(index).repeat();
				}
			}
		}
		
		public void print_sorted_list() {
			for(DiscreetData e : sorted_values) {
				System.out.print(e.value);
				System.out.print("  ");
				System.out.println(e.repetition);
				
			}
		}
	}
	
	private static class ContinualDataTable{
		private int count;
		private ArrayList<ContinualData> data = null;
		
		public ContinualDataTable() {
			data = new ArrayList<ContinualData>();
			this.count = 0;
		}
		
		private int find_range(float[] range) {
			for(int i = 0; i < count; i++) {
				if(data.get(i).range == range) return i;
			}
			return -1;
		}
		
		public void add(float[] range, int num) {
			int index = find_range(range);
			if(index == -1) {
				data.add(new ContinualData(range, num));
				count++;
			}else {
				data.get(index).samples += num;
			}
		}
		
		public void print() {
			System.out.printf("Number of rows: %d%n", count);
			for(ContinualData e : data) {
				System.out.printf("[%f - %f] : %d%n", e.range[0], e.range[1], e.samples);
			}
		}
		
	}
	
	private static Scanner input = new Scanner(System.in);
	
	public static void main(String[] args) {
		int choice = 0;
		do {
			System.out.print("Choose sample data(1 - discreet, 2 - continual): ");
			choice = input.nextInt();
			if(choice != 1 && choice != 2) System.out.println("Option unavailable!");
		}while(choice != 1 && choice != 2);
		
		String buffer = input.nextLine();
		if(choice == 1) {
			System.out.println("Input sample data: ");
			String input_string = input.nextLine();
			ArrayList<String> string_list = new ArrayList<String>();
			String[] split_string = input_string.split(" ");
			for(int i = 0; i < split_string.length; i++)
				if(split_string[i] != "") string_list.add(split_string[i]);
	
			ArrayList<Float> values = new ArrayList<Float>(string_list.size());
			for(String str : string_list)
				values.add(Float.parseFloat(str));
			
			DiscreetDataTable table = new DiscreetDataTable(values);
			table.print_sorted_list();
			
			float average = get_average(table);
			double deviation = get_variance(table);
			
			System.out.println("Average: " + Float.toString(average));
			System.out.println("Variance: " + Double.toString(deviation));
		}
		
		if(choice == 2) {
			System.out.print("Input number of rows: ");
			int colcount = input.nextInt();
			ContinualDataTable table = new ContinualDataTable();
			for(int i = 0; i < colcount; i++) {
				System.out.printf("Input range of row #%d (lower, upper): ", i + 1);
				float[] range = new float[2];
				range[0] = input.nextFloat();
				range[1] = input.nextFloat();
				System.out.printf("Input number of samples in the range of row #%d: ", i + 1);
				int samples = input.nextInt();
				table.add(range, samples);
			}
			
			System.out.println();
			table.print();
			System.out.println();

			float average = get_average(table);
			float deviation = get_variance(table);
			
			System.out.println("Average: " + Float.toString(average));
			System.out.println("Variance: " + Float.toString(deviation));
		}
	}
	
	public static float get_average(DiscreetDataTable data) {
		float res = 0;
		for(DiscreetData e : data.sorted_values){
			res += e.value * e.repetition;
		}
		res /= data.count;
		return res;
	}

	public static float get_average(ContinualDataTable data) {
		float res = 0, count = 0;
		for(ContinualData e : data.data){
			float mid = (e.range[0] + e.range[1]) / 2;
			res += mid * e.samples;
			count += e.samples;
		}
		res /= count;
		return res;
	}
	
	public static double get_variance(DiscreetDataTable data) {
		double res = 0;
		float average = get_average(data);
		//System.out.println(average);
		for(DiscreetData e : data.sorted_values) {
			//System.out.printf("%f %n",Math.pow(e.value - average, 2) * e.repetition);
			res += Math.pow(e.value - average, 2) * e.repetition;
			//System.out.println(res);
		}
		res /= data.count - 1;
		return res;
	}
	
	public static float get_variance(ContinualDataTable data) {
		float res = 0, count = 0;
		float average = get_average(data);
		for(ContinualData e : data.data){
			float mid = (e.range[0] + e.range[1]) / 2;
			res += Math.pow(mid - average, 2) * e.samples;
			count += e.samples;
		}
		res /= count - 1;
		return res;
	}
	
}
