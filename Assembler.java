import java.util.HashMap;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.io.FileWriter;
import java.io.IOException;

public class Assembler{
    private static HashMap<String, String> instructions = new HashMap<>();
    private static Set<String> load_store_format = new HashSet<>();
    private static Set<String> load_index_format = new HashSet<>();
    private static Set<String> immed = new HashSet<>();
    private static Set<String> r_immed = new HashSet<>();
    private static Set<String> register_ops = new HashSet<>();
    private static Set<String> shift_rotate = new HashSet<>();
    private static Set<String> io_ops = new HashSet<>();

    private static int location = 0;
    private static Scanner scanner;
    private static FileWriter listing_writer;
    private static FileWriter loading_writer;
    private static HashMap<String, String> labelDict = new HashMap<>();

    public static void main(String[] args) throws IOException {
        initializeInstructions();
        initializeFiles();
        initialize_load();
        initialize_r_immed();
        initialize_inde();
        initialize_io_ops();
        initialize_register_ops();
        initialize_shift_rotate();
        initialize_immed();
        
        firstPass();
        System.out.println(labelDict);

        scanner.close();
        listing_writer.close();
        loading_writer.close();
        initializeFiles();
        secondPass();
        scanner.close();
        listing_writer.close();
        loading_writer.close();
    }

    public static void firstPass(){
        while(scanner.hasNextLine()){
            String data = scanner.nextLine();
            String[] dataArr = data.split("\t");
            int location_change = location;
            for(int i = 0; i < dataArr.length; i++){
                String curWord = dataArr[i];
                if(curWord.equals("LOC")){
                    location_change = Integer.parseInt(dataArr[i+1]);
                }
                if(curWord != ""){
                    if(curWord.charAt(curWord.length()-1) == ':'){
                        labelDict.put(curWord.substring(0, curWord.length()-1), String.valueOf(location));
                    }
                }
            }
            location++;
            location = location_change;
        }
        location = 0;
    }

    public static void secondPass(){
        while(scanner.hasNextLine()){
            ArrayList<String> listing_out = new ArrayList<String>();
            String data = scanner.nextLine();
            String[] dataArr = data.split("\t");
            String[] split_operands;
            String label = dataArr[0];
            String operation = dataArr[1];
            String operands = "";
            
            if(dataArr.length > 2){
                operands = dataArr[2];
            }

            String comment = "";
            String instruction = "";
            String register = "";
            String indexing = "";
            String address = "";
            String indirect = "0";
            String binary_string = "";

            if(dataArr[dataArr.length-1].charAt(0) == ';'){
                comment = dataArr[dataArr.length-1];
            }
            if(!(operation.equals("LOC"))){
                split_operands = operands.split(",");
                int octal_loc = decToOctal(String.valueOf(location));
                listing_out.add(String.format("%06d",octal_loc) + '\t');

                if(load_store_format.contains(operation)){
                    register = split_operands[0].strip();
                    indexing = split_operands[1].strip();
                    address = split_operands[2].strip();
                    if(split_operands.length > 3){
                        indirect = split_operands[3].strip();
                    }
                    binary_string += instructions.get(operation).substring(0, 6);
                    binary_string += String.format("%02d",decToBinary(register));
                    binary_string += String.format("%02d",decToBinary(indexing));
                    binary_string += indirect;
                    binary_string += String.format("%05d",decToBinary(address));
                    instruction = String.format("%06d",binToOctal(binary_string));

                }else if(load_index_format.contains(operation)){
                    indexing = split_operands[0].strip();
                    address = split_operands[1].strip();
                    indirect = "0";
                    if(split_operands.length > 2){
                        indirect = split_operands[2].strip();
                    }
                    binary_string += instructions.get(operation).substring(0, 6);
                    binary_string += "00";
                    binary_string += String.format("%02d",decToBinary(indexing));
                    binary_string += indirect;
                    binary_string += String.format("%05d",decToBinary(address));

                    instruction = String.format("%06d",binToOctal(binary_string));            
                }else if(immed.contains(operation)){
                    register = "00";
                    indexing = "00";
                    indirect = "0";
                    address = split_operands[0].strip();

                    binary_string += instructions.get(operation).substring(0, 6);
                    binary_string += register;
                    binary_string += indexing;
                    binary_string += indirect;
                    binary_string += String.format("%05d",decToBinary(address));
                    instruction = String.format("%06d",binToOctal(binary_string));
                    
                }else if(r_immed.contains(operation)){
                    register = split_operands[0].strip();
                    indexing = "00";
                    indirect = "0";
                    address = split_operands[1].strip();

                    binary_string += instructions.get(operation).substring(0, 6);
                    binary_string += String.format("%02d",decToBinary(register));
                    binary_string += indexing;
                    binary_string += indirect;
                    binary_string += String.format("%05d",decToBinary(address));
                    instruction = String.format("%06d",binToOctal(binary_string));
                    
                }else if(register_ops.contains(operation)){
                    String registerx = split_operands[0].strip();
                    String registery = split_operands[1].strip();

                    binary_string += instructions.get(operation).substring(0, 6);
                    binary_string += String.format("%02d",decToBinary(registerx));
                    binary_string += String.format("%02d",decToBinary(registery));
                    binary_string += "000000";
                    instruction = String.format("%06d",binToOctal(binary_string));
                }else if(operation.equals("NOT")){
                    register = split_operands[0].strip();
                    binary_string += instructions.get(operation).substring(0,6);
                    binary_string += String.format("%02d", decToBinary(register));
                    binary_string += "00000000";
                    instruction = String.format("%06d", binToOctal(binary_string));
                }else if(shift_rotate.contains(operation)){
                    register = split_operands[0].strip();
                    String count = split_operands[1].strip();
                    String lr = split_operands[2].strip();
                    String al = split_operands[3].strip();

                    binary_string += instructions.get(operation).substring(0, 6);
                    binary_string += String.format("%02d",decToBinary(register));
                    binary_string += String.format("%1d",decToBinary(al));
                    binary_string += String.format("%1d",decToBinary(lr));
                    binary_string += "00";
                    binary_string += String.format("%04d",decToBinary(count));
                    instruction = String.format("%06d",binToOctal(binary_string));
                }else if(io_ops.contains(operation)){
                    register = split_operands[0].strip();
                    String devid = split_operands[1].strip();
                    binary_string += instructions.get(operation).substring(0,6);
                    binary_string += String.format("%02d", decToBinary(register));
                    binary_string += "000";
                    binary_string += String.format("%05d",decToBinary(devid));
                    instruction = String.format("%06d", binToOctal(binary_string));
                    
                }else if(operation.equals("Data")){
                    if(labelDict.containsKey(split_operands[0])){
                        listing_out.add(String.format("%06d",decToOctal(labelDict.get(split_operands[0]))));
                    }else{
                        listing_out.add(String.format("%06d",decToOctal(split_operands[0])));
                    }
                }else if(operation.equals("LOC")){
                    location = Integer.parseInt(operands);
                }else if(operation.equals("HLT")){
                    instruction = "000000";
                }

                listing_out.add(instruction + '\t');
                listing_out.add(label + '\t');
                listing_out.add(operation + '\t');
                listing_out.add(operands + '\t');
                listing_out.add(comment + '\t');

                for(int i = 0; i < listing_out.size(); i++){
                    try {
                        listing_writer.write(listing_out.get(i));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    listing_writer.write('\n');
                } catch (IOException e) {
                    e.printStackTrace();
                }
                location++;
                if((listing_out.get(0) != "") && (listing_out.get(1) != "")){
                    try {
                        loading_writer.write(listing_out.get(0) + '\t' + listing_out.get(1) + '\n');
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }else{
                listing_out.add("" + '\t');
                listing_out.add("" + '\t');
                listing_out.add("" + '\t');
                listing_out.add("LOC" + '\t');
                listing_out.add(dataArr[2] + '\t');
                listing_out.add(comment + '\t');
                for(int i = 0; i < listing_out.size(); i++){
                    try {
                        listing_writer.write(listing_out.get(i));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    listing_writer.write('\n');
                } catch (IOException e) {
                    e.printStackTrace();
                }
                location = Integer.parseInt(dataArr[2]);
            }   
        }
    }

    public static void initializeFiles(){
        try {
            File file = new File("SOURCE.txt");
            scanner = new Scanner(file);
        }catch(FileNotFoundException e){
            e.printStackTrace();
            System.exit(1);
        }

        try{
            loading_writer = new FileWriter("LOADING.txt");
        }catch(IOException e){
            e.printStackTrace();
            System.exit(1);
        }

        try{
            listing_writer = new FileWriter("LISTING.txt");
        }catch(IOException e){
            e.printStackTrace();
            System.exit(1);
        }
    }
    public static void initialize_load(){
        load_store_format.add("LDR");
        load_store_format.add("STR");
        load_store_format.add("LDA");
        load_store_format.add("JZ");
        load_store_format.add("JNE");
        load_store_format.add("SOB");
        load_store_format.add("JGE");
        load_store_format.add("JCC");
        load_store_format.add("AMR");
        load_store_format.add("SMR");
    }
    public static void initialize_inde(){
        load_index_format.add("LDX");
        load_index_format.add("STX");
        load_index_format.add("JMA");
        load_index_format.add("JSR");
    }

    public static void initialize_immed(){
        immed.add("RFS");
    }
    public static void initialize_r_immed(){

        r_immed.add("AIR");
        r_immed.add("SIR");
    }
    public static void initialize_register_ops(){
        register_ops.add("MLT");
        register_ops.add("DVD");
        register_ops.add("TRR");
        register_ops.add("AND");
        register_ops.add("ORR");
    }
    public static void initialize_shift_rotate(){
        shift_rotate.add("SRC");
        shift_rotate.add("RRC");
    }
    public static void initialize_io_ops(){
        io_ops.add("IN");
        io_ops.add("OUT");
        io_ops.add("CHK");
    }
    public static void initializeInstructions(){
        instructions.put("HLT", "000000");
        instructions.put("LDR", "000001");
        instructions.put("STR", "000010");
        instructions.put("LDA", "000011");
        instructions.put("LDX", "100001");
        instructions.put("STX", "010010");
        instructions.put("JZ", "001000");
        instructions.put("JNE", "001001");
        instructions.put("JCC", "001010");
        instructions.put("JMA", "001011");
        instructions.put("JSR", "001100");
        instructions.put("RFS", "001101");
        instructions.put("SOB", "001110");
        instructions.put("JGE", "001111");
        instructions.put("AMR", "000100");
        instructions.put("SMR", "000101");
        instructions.put("AIR", "000110");
        instructions.put("SIR", "000111");
        instructions.put("MLT", "111000");
        instructions.put("DVD", "111010");
        instructions.put("TRR", "111011");
        instructions.put("AND", "111011");
        instructions.put("ORR", "111100");
        instructions.put("NOT", "111101");
        instructions.put("SRC", "011001");
        instructions.put("RCC", "011010");
        instructions.put("IN", "110001");
        instructions.put("OUT", "110010");
        instructions.put("CHK", "110011");
        instructions.put("Data", "");
        instructions.put("LOC","");
    }

    public static int decToOctal(String string_n)
    {
        int n = Integer.parseInt(string_n);

        int[] octalNum = new int[100];
        int i = 0;
        while (n != 0) {
            octalNum[i] = n % 8;
            n = n / 8;
            i++;
        }
        String return_num = "";
        for (int j = i - 1; j >= 0; j--)
            return_num += octalNum[j];

        if(return_num.equals("")){
            return 0;
        }else{
            return Integer.parseInt(return_num);
        }
    }
    public static int decToBinary(String string_input)
    {
        int n = Integer.parseInt(string_input);
        int[] binaryNum = new int[1000];
        int i = 0;
        while (n > 0) 
        {
            binaryNum[i] = n % 2;
            n = n / 2;
            i++;
        }
        String return_num = "";
        for (int j = i - 1; j >= 0; j--)
        return_num +=  binaryNum[j];
        if(return_num.equals("")){
            return 0;
        }else{
            return Integer.parseInt(return_num);
        }
    }
    public static int binToOctal(String binaryNumber_string) {
        int decimalNumber = Integer.parseInt(binaryNumber_string,2);
        int octalNumber = 0, i = 0;
    
        i = 1;
    
        while (decimalNumber != 0) {
          octalNumber += (decimalNumber % 8) * i;
          decimalNumber /= 8;
          i *= 10;
        }
    
        return octalNumber;
      }
}

