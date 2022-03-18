// Made by VGenesis

package engine;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.io.FileNotFoundException;

public class engine {
	private enum STATE_BASIC{
		INITIALIZE,
		INPUT_DATA,
		CALCULATE_BASIC_STATISTICS,
		PRINT_BASIC_STATISTICS,
		CALCULATE_DISCREET_DATA,
		PRINT_DISCREET_DATA,
		CALCULATE_CONTINUAL_DATA,
		PRINT_CONTINUAL_DATA,
		END
	}
	
	private enum STATE_BIPARAMETER{
		INITIALIZE,
		INPUT_DATA,
		CALCULATE_MARGINAL_PROBABILITIES,
		PRINT_RESULTS,
		END
	}
	
	private enum STATE_REGRESSION{
		INITIALIZE,
		INPUT_DATA,
		CALCULATE_REGRESSION,
		PRINT,
		END
	}
	
	private static class DiscreetData{
		private class DiscreetDataPack{
			float data;
			int count;
			DiscreetDataPack next;
			
			public DiscreetDataPack(float data, int count, DiscreetDataPack next) {
				this.data = data;
				this.count = count;
				this.next = next;
			}
		}
		
		DiscreetDataPack head;
		
		DiscreetData(){
			this.head = null;
		}
		
		void add(float data) {
			if(head == null) {
				head = new DiscreetDataPack(data, 1, null);
				return;
			}
			DiscreetDataPack iterator = head;
			while(iterator.next != null && iterator.data != data) iterator = iterator.next;
			if(iterator.data == data) iterator.count++;
			else if(iterator.next == null) iterator.next = new DiscreetDataPack(data, 1, null);
		}
		
		void sort() {
			int n = 0;
			DiscreetDataPack iterator = head;
			while(iterator != null) {
				n++;
				iterator = iterator.next;
			}
			
			DiscreetDataPack[] data_list = new DiscreetDataPack[n];
			int i = 0;
			iterator = head;
			while(iterator != null) {
				data_list[i] = iterator;
				iterator = iterator.next;
				data_list[i].next = null;
				i++;
			}
			
			for(i = 0; i < n - 1; i++) {
				for(int j = i + 1; j < n; j++) {
					if(data_list[i].data > data_list[j].data) {
						DiscreetDataPack temp = data_list[i];
						data_list[i] = data_list[j];
						data_list[j] = temp;
					}
				}
			}
			
			head = data_list[0];
			iterator = head;
			for(i = 1; i < n; i++) {
				iterator.next = data_list[i];
				iterator = iterator.next;
			}
		}
		
		@Override
		public String toString() {
			String res = "";
			DiscreetDataPack iterator = head;
			while(iterator != null) {
				res += iterator.data + " : " + iterator.count + '\n';
				iterator = iterator.next;
			}
			return res;
		}
	}

	private static class ContinualData{
		private static class ContinualDataPack{
			float[] value_range;
			int count;
			ContinualDataPack next;
			
			ContinualDataPack(float range_min, float range_max, int count, ContinualDataPack next){
				this.value_range = new float[2];
				this.value_range[0] = range_min;
				this.value_range[1] = range_max;
				this.count = count;
				this.next = next;
			}
		}
		
		ContinualDataPack head;
		
		ContinualData(float min_range, float max_range, int segments){
			float delta_range = (max_range - min_range) / segments;
			float start_range = min_range;
			head = new ContinualDataPack(start_range, start_range + delta_range, 0, null);
			ContinualDataPack iterator = head;
			for(int i = 1; i < segments; i++) {
				start_range += delta_range;
				ContinualDataPack pack = new ContinualDataPack(start_range, start_range + delta_range, 0, null);
				iterator.next = pack;
				iterator = iterator.next;
				
			}
		}
		
		void add(float data) {
			ContinualDataPack iterator = head;
			while(iterator != null && (data < iterator.value_range[0] || data > iterator.value_range[1]))
				iterator = iterator.next;
			if(iterator != null)iterator.count++;
		}
		
		@Override
		public String toString() {
			String res = "";
			ContinualDataPack iterator = head;
			while(iterator != null) {
				res += iterator.value_range[0] + " - " + iterator.value_range[1] + " : " + iterator.count + '\n';
				iterator = iterator.next;
			}
			return res;
		}
	}

	private static String input_filepath = "D:/Tools/Eclipse/StatisticsEngine/src/engine/input.txt";
	private static String output_filepath = "D:/Tools/Eclipse/StatisticsEngine/src/engine/output.txt";
	
	public static void calculate_basic() {
		STATE_BASIC state = STATE_BASIC.INITIALIZE;
		int data_packet_size = 0;
		float[] data_packet = new float[data_packet_size];
		float min_data = 0;
		float max_data = 0;
		int segments = 0;
		float segment_size = 0;
		float median = 0;
		double dispersion = 0;
		DiscreetData discreet_data = new DiscreetData();
		ContinualData continual_data = null;
		while(true)
			switch(state) {
			case INITIALIZE:
				initialize();
				state = STATE_BASIC.INPUT_DATA;
				break;
			case INPUT_DATA:
				try {
					FileReader reader = new FileReader(input_filepath);
					String data_raw = "";
					int i = 0;
					while((i = reader.read()) != -1) data_raw += (char)i;
					reader.close();
					String[] data_bits = data_raw.split(" ");
					data_packet_size = data_bits.length;
					data_packet = new float[data_packet_size];
					for(i = 0; i < data_packet_size; i++) if(!data_bits[i].isBlank()) data_packet[i] = Float.parseFloat(data_bits[i]);
					state = STATE_BASIC.CALCULATE_BASIC_STATISTICS;
				}catch(IOException e2) {
					System.out.println("Cannot read file.");
				}
				break;
			case CALCULATE_BASIC_STATISTICS:
				min_data = data_packet[0];
				max_data = data_packet[0];
				for(float data : data_packet) {
					median += data;
					min_data = Math.min(data, min_data);
					max_data = Math.max(data, max_data);
				}
				median /= data_packet_size;
				for(float data : data_packet) dispersion += Math.pow(median - data, 2);
				dispersion /= data_packet_size - 1;
				segments = (int)Math.round((double)(1.0 + 3.322 * Math.log10(data_packet_size)));
				segment_size = (max_data - min_data) / segments;
				state = STATE_BASIC.PRINT_BASIC_STATISTICS;
				break;
			case PRINT_BASIC_STATISTICS:
					try {
					FileWriter writer = new FileWriter(output_filepath, true);
					writer.write("MIN VALUE: " + min_data + "\n");
					writer.write("MAX VALUE: " + max_data + "\n");
					writer.write("DATA PACKET SIZE: " + data_packet_size + "\n");
					writer.write("# OF SEGMENTS: " + segments + "\n");
					writer.write("SEGMENT SIZE: " + segment_size + "\n");
					writer.write("MEDIAN: " + median + "\n");
					writer.write("DISPERSION: " + dispersion + "\n");
					writer.write('\n');
					writer.close();
					state = STATE_BASIC.CALCULATE_DISCREET_DATA;
				}catch(IOException e) {
					System.out.println("Unhandled IO error during data write.");
				}
				break;
			case CALCULATE_DISCREET_DATA:
				for(float data : data_packet) discreet_data.add(data);
				discreet_data.sort();
				state = STATE_BASIC.PRINT_DISCREET_DATA;
				break;
			case PRINT_DISCREET_DATA:
				try {
					FileWriter writer = new FileWriter(output_filepath, true);
					String data = discreet_data.toString();
					writer.write(data);
					writer.write('\n');
					writer.close();
					state = STATE_BASIC.CALCULATE_CONTINUAL_DATA;
				}catch(IOException e) {
					System.out.println("Unhandled IO error during data write.");
				}
				break;
			case CALCULATE_CONTINUAL_DATA:
				continual_data = new ContinualData(min_data, max_data, segments);
				for(float data : data_packet) continual_data.add(data);
				state = STATE_BASIC.PRINT_CONTINUAL_DATA;
				break;
			case PRINT_CONTINUAL_DATA:
				try {
					FileWriter writer = new FileWriter(output_filepath, true);
					String data = continual_data.toString();
					writer.write(data);
					writer.write('\n');
					writer.close();
					state = STATE_BASIC.END;
				}catch(IOException e) {
					System.out.println("Unhandled IO error during data write.");
				}
				break;
			case END:
				return;
			}
	}
	
	public static void calculate_biparameter() {
		STATE_BIPARAMETER state = STATE_BIPARAMETER.INITIALIZE;
		float[][] data_packet = new float[0][0];
		int[] data_packet_size = new int[2];
		float[] marginal_probabilities_row = new float[0];
		float[] marginal_probabilities_column = new float[0];
		float[][] marginal_probabilities = new float[0][0];
		float[][] marginal_frequencies = new float[0][0];
		int sample_size = 0;
		float dispersion = 0;
		while(true) {
			switch(state) {
			case INITIALIZE:
				initialize();
				state = STATE_BIPARAMETER.INPUT_DATA;
				break;
			case INPUT_DATA:
				try {
					FileReader reader = new FileReader(input_filepath);
					String raw_text = "";
					int i;
					while((i = reader.read()) != -1) raw_text += (char)i;
					reader.close();
					String[] segmented_text = raw_text.split("\n");
					String[] data_line = segmented_text[0].split(" ");
					data_packet = new float[segmented_text.length][data_line.length];
					for(i = 0; i < segmented_text.length; i++) {
						data_line = segmented_text[i].split(" ");
						int j = 0;
						for(String word : data_line) data_packet[i][j++] = Float.parseFloat(word);
						
					}
					data_packet_size[0] = data_packet.length;
					data_packet_size[1] = data_packet[0].length;
					state = STATE_BIPARAMETER.CALCULATE_MARGINAL_PROBABILITIES;
					break;
				}catch(FileNotFoundException e0) {
					File f = new File(input_filepath);
					try {
						f.createNewFile();
					}catch(IOException e2) {
						System.out.println("Cannot create file at " + input_filepath + ". Access Denied.");
					}
				}catch(IOException e1) {
					System.out.println("Cannot read file at " + input_filepath);
				}
			case CALCULATE_MARGINAL_PROBABILITIES:
				float[] marginal_frequencies_row = new float[data_packet_size[1]];
				float[] marginal_frequencies_column = new float[data_packet_size[0]];
				sample_size = 0;
				for(int i = 0; i < data_packet_size[1]; i++) {
					for(int j = 0; j < data_packet_size[0]; j++) {
						marginal_frequencies_row[i] += data_packet[i][j];
					}
				}
				for(int i = 0; i < data_packet_size[0]; i++) {
					for(int j = 0; j < data_packet_size[1]; j++) {
						marginal_frequencies_column[i] += data_packet[j][i];
						sample_size += data_packet[i][j];
					}
				}
				marginal_probabilities_row = new float[data_packet_size[1]];
				marginal_probabilities_column = new float[data_packet_size[0]];
				for(int i = 0; i < data_packet_size[1]; i++) marginal_probabilities_row[i] = marginal_frequencies_row[i] / sample_size;
				for(int i = 0; i < data_packet_size[0]; i++) marginal_probabilities_column[i] = marginal_frequencies_column[i] / sample_size;
				
				marginal_probabilities = new float[data_packet_size[0]][data_packet_size[1]];
				marginal_frequencies = new float[data_packet_size[0]][data_packet_size[1]];
				for(int i = 0; i < data_packet_size[0]; i++) {
					for(int j = 0; j < data_packet_size[1]; j++) {
						marginal_probabilities[i][j] = marginal_probabilities_row[j] * marginal_probabilities_column[i];
						marginal_frequencies[i][j] = marginal_probabilities[i][j] * sample_size;
						dispersion += Math.pow(marginal_frequencies[i][j] - data_packet[i][j], 2) / marginal_frequencies[i][j];
					}
				}
				dispersion = (float)Math.sqrt(dispersion);
				dispersion /= data_packet_size[0] - 1;
				
				state = STATE_BIPARAMETER.PRINT_RESULTS;
			
			case PRINT_RESULTS:
				try {
					FileWriter writer = new FileWriter(output_filepath, true);
					writer.write("INPUT DATA: \n");
					for(float[] line : data_packet) {
						for(float value : line) {
							writer.write(Float.toString(value));
							writer.write(" ");
						}
						writer.write('\n');
					}
					writer.write('\n');
					writer.write("MARGINAL FREQUENCIES(mirrored over diagonal): \n");
					for(float[] line : marginal_frequencies) {
						for(float value : line) {
							writer.write(Float.toString(value));
							writer.write(" ");
						}
						writer.write('\n');
					}
					writer.write('\n');
					writer.write("SAMPLE SIZE: " + sample_size);
					writer.write('\n');
					writer.write("DISPERSION: " + dispersion);
					writer.close();
					state = STATE_BIPARAMETER.END;
				}catch(IOException e) {
					System.out.println("Unhandled IO error during data write.");
				}
				break;
			case END:
				return;
			}
		}
	}
	
	public static void calculate_regression() {
		while(true) {
			STATE_REGRESSION state = STATE_REGRESSION.INITIALIZE;
			float[][] data_packet = new float[0][0];
			int[] data_packet_size = new int[2];
			switch(state) {
			case INITIALIZE:
				initialize();
				state = STATE_REGRESSION.INPUT_DATA;
				break;
			case INPUT_DATA:
				try {
					FileReader reader = new FileReader(input_filepath);
					String raw_text = "";
					int i;
					while((i = reader.read()) != -1)raw_text += i;
					reader.close();
					
					String[] raw_text_lines = raw_text.split("\n");
					String[] temp_data_line = raw_text_lines[0].split(" ");
					String[][] data_text = new String[raw_text_lines.length][temp_data_line.length];
					
					i = 0;
					for(String line : raw_text_lines)data_text[i++] = line.split(" ");
					for(i = 0; i < data_packet_size[0]; i++) {
						for(int j = 0; j < data_packet_size[1]; j++) {
							data_packet[i][j] = Float.parseFloat(data_text[i][j]);
						}
					}
					
					data_packet_size[0] = raw_text_lines.length;
					data_packet_size[1] = temp_data_line.length;
					state = STATE_REGRESSION.CALCULATE_REGRESSION;
				}catch(IOException e) {
					System.out.println("Cannot read file.");
				}
				break;
			case CALCULATE_REGRESSION:
				
				break;
			case PRINT:
				break;
			case END:
				break;
					
			}
		}
	}
	
	public static void main(String[] args) {
		calculate_biparameter();
	}
	
	public static void initialize() {
		try {
			File input = new File(input_filepath);
			if(!input.exists()) input.createNewFile();
			if(!input.canRead()) System.out.println("Cannot read input file.");
			FileReader reader = new FileReader(input);
			if(reader.read() == -1) System.out.println("Input file is empty.");
			reader.close();
			
			File output = new File(output_filepath);
			if(!output.exists()) output.createNewFile();
			if(!output.canWrite()) System.out.println("Cannot write output file.");
			FileWriter writer = new FileWriter(output, false);
			writer.close();
		}catch(IOException e) {
			System.out.println("IO error.");
		}
	}
}
