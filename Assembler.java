import java.util.HashMap;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;

public class Assembler{
    private static HashMap<String, String> instructions = new HashMap<>();
    private static Set<String> load_store = new HashSet<>();
    private static Set<String> load_index = new HashSet<>();
    private static Set<String> misc = new HashSet<>();
    private static Set<String> transfer = new HashSet<>();
    private static Set<String> alu = new HashSet<>();
    private static Set<String> register_ops = new HashSet<>();
    private static Set<String> shift_rotate = new HashSet<>();
    private static Set<String> io_ops = new HashSet<>();

    private static int location = 0;
    private static Scanner scanner;
    private static HashMap<String, String> labelDict = new HashMap<>();

    public static void main(String[] args) {
        initializeInstructions();
        initializeFile();
        initialize_load();
        initialize_misc();
        initialize_alu();
        initialize_inde();
        initialize_io_ops();
        initialize_register_ops();
        initialize_shift_rotate();
        initialize_transfer();
        
        firstPass();
        //labelDict.forEach((key, value) -> System.out.println(key + " " + value));

        scanner.close();
        initializeFile();
        secondPass();
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
            String operands = dataArr[2];
            String instruction = "";
            if(!(operation.equals("LOC"))){
                split_operands = operands.split(",");
                int octal_loc = decToOctal(location);
                listing_out.add(String.format("%06d",octal_loc) + '\t');

                if(load_store.contains(operation)){
                    String register = split_operands[0].strip();
                    String indexing = split_operands[1].strip();
                    String address = split_operands[2].strip();
                    String indirect = "0";
                    if(split_operands.length > 3){
                        indirect = split_operands[3].strip();
                    }
                    String binary_string = "";
                    binary_string += instructions.get(operation).substring(0, 6);
                    binary_string += String.format("%02d",decToBinary(Integer.parseInt(register)));
                    binary_string += String.format("%02d",decToBinary(Integer.parseInt(indexing)));
                    binary_string += indirect;
                    binary_string += String.format("%05d",decToBinary(Integer.parseInt(address)));
                    instruction = String.format("%06d",decToOctal(Integer.parseInt(binary_string,2)));
                }else if(load_index.contains(operation)){
                    String indexing = split_operands[0].strip();
                    String address = split_operands[1].strip();
                    String indirect = "0";
                    if(split_operands.length > 2){
                        indirect = split_operands[2].strip();
                    }
                    String binary_string = "";
                    binary_string += instructions.get(operation).substring(0, 6);
                    binary_string += "00";
                    binary_string += String.format("%02d",decToBinary(Integer.parseInt(indexing)));
                    binary_string += indirect;
                    binary_string += String.format("%05d",decToBinary(Integer.parseInt(address)));
                    instruction = String.format("%06d",decToOctal(Integer.parseInt(binary_string,2)));
                }else if(misc.contains(operation)){
                    
                }else if(transfer.contains(operation)){
                    // String register = split_operands[0].strip();
                    // String indexing = split_operands[1].strip();
                    // String address = split_operands[2].strip();
                    // String indirect = "0";
                    // if(split_operands.length > 3){
                    //     indirect = split_operands[3].strip();
                    // }
                    // String binary_string = "";
                    // binary_string += instructions.get(operation).substring(0, 6);
                    // binary_string += String.format("%02d",decToBinary(Integer.parseInt(register)));
                    // binary_string += String.format("%02d",decToBinary(Integer.parseInt(indexing)));
                    // binary_string += indirect;
                    // binary_string += String.format("%05d",decToBinary(Integer.parseInt(address)));
                    // instruction = String.format("%06d",decToOctal(Integer.parseInt(binary_string,2)));
                }else if(alu.contains(operation)){

                }else if(register_ops.contains(operation)){

                }else if(shift_rotate.contains(operation)){

                }else if(io_ops.contains(operation)){

                }else if(operation.equals("Data")){
                    if(labelDict.containsKey(split_operands[0])){
                        listing_out.add(String.format("%06d",decToOctal(Integer.parseInt(labelDict.get(split_operands[0])))));
                    }else{
                        listing_out.add(String.format("%06d",decToOctal(Integer.parseInt(split_operands[0]))));
                    }
                }else if(operation.equals("LOC")){
                    location = Integer.parseInt(operands);
                }

                listing_out.add(instruction + '\t');
                listing_out.add(label + '\t');
                listing_out.add(operation + '\t');
                listing_out.add(operands + '\t');
                
                if(dataArr.length > 5){
                    listing_out.add(dataArr[5]);
                }

                for(int i = 0; i < listing_out.size(); i++){
                    System.out.print(listing_out.get(i));
                }
                System.out.print('\n');
                location++;
            }else{
                listing_out.add("" + '\t');
                listing_out.add("" + '\t');
                listing_out.add("" + '\t');
                listing_out.add("LOC" + '\t');
                listing_out.add(dataArr[2] + '\t');
                for(int i = 0; i < listing_out.size(); i++){
                    System.out.print(listing_out.get(i));
                }
                System.out.print('\n');
                location = Integer.parseInt(dataArr[2]);
            }   
        }
    }
    // public static String[] process_line(String[] dataArr, int location){
    //     String[] listingOut = new String[6];
    //     boolean operandNext = false;
    //     boolean directive = false;

    //     for(int i = 0; i < 4; i++){
    //         if(i < dataArr.length){
    //             String curWord = dataArr[i];

    //             if(operandNext){
    //                 listingOut[4] = curWord;
    //                 if(directive){
    //                     directive = false;
    //                 }else{
    //                     listingOut[1] = curWord;
    //                 }
    //                 operandNext = false;
    //             }else{
    //                 if(!curWord.equals("")){
    //                     if(curWord.charAt(curWord.length()-1) == ':'){
    //                             labelDict.put(curWord.substring(0, curWord.length()-1), String.valueOf(location));
    //                             listingOut[2] = curWord; 
    //                     }else if(curWord.charAt(0) == ';'){
    //                             listingOut[5] = curWord;
    //                     }else{
    //                         listingOut[3] = curWord;
    //                         if(curWord.equals("LOC")){
    //                             directive = true;
    //                             operandNext = true;
    //                         }else if(!curWord.equals("HLT")){
    //                             listingOut[0] = instructions.get(curWord);
    //                             operandNext = true;
    //                         }else{
    //                             listingOut[0] = instructions.get(curWord);
    //                         }
                            
    //                     }
    //                 }
    //             }
    //         }
    //     }
    //     return listingOut;
    // }

    public static void initializeFile(){
        try {
            File file = new File("SOURCE.txt");
            scanner = new Scanner(file);
        }catch(FileNotFoundException e){
            e.printStackTrace();
            System.exit(1);
        }
    }
    public static void initialize_load(){
        load_store.add("LDR");
        load_store.add("STR");
        load_store.add("LDA");
    }

    public static void initialize_inde(){
        load_index.add("LDX");
        load_store.add("STX");
    }

    public static void initialize_misc(){
        misc.add("HLT");
        misc.add("TRAP");
    }
    public static void initialize_transfer(){
        transfer.add("JZ");
        transfer.add("JNE");
        transfer.add("JCC");
        transfer.add("JMA");
        transfer.add("JSR");
        transfer.add("RFS");
        transfer.add("SOB");
        transfer.add("JGE");
    }
    public static void initialize_alu(){
        alu.add("AMR");
        alu.add("SMR");
        alu.add("AIR");
        alu.add("SIR");
    }
    public static void initialize_register_ops(){
        register_ops.add("MLT");
        register_ops.add("DVD");
        register_ops.add("TRR");
        register_ops.add("AND");
        register_ops.add("ORR");
        register_ops.add("NOT");
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
        instructions.put("TRAP", "011000");
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
        instructions.put("SIR", "111001");
        instructions.put("MLT", "111000");
        instructions.put("DVD", "111010");
        instructions.put("TRR", "111011");
        instructions.put("AND", "111011");
        instructions.put("ORR", "111100");
        instructions.put("NOT", "111101");
        instructions.put("SRC", "011001");
        instructions.put("RCC", "011010");
        instructions.put("FADD", "011011");
        instructions.put("FSUB", "011100");
        instructions.put("VADD", "011101");
        instructions.put("VSUB", "011110");
        instructions.put("CNVRT", "011111");
        instructions.put("LDFR", "101000");
        instructions.put("STFR", "101001");
        instructions.put("Data", "");
        instructions.put("LOC","");
    }

    public static int decToOctal(int n)
    {
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
    public static int decToBinary(int n)
    {
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

    public static int binToOctal(int binaryNumber) {
        int octalNumber = 0, decimalNumber = 0, i = 0;
    
        while (binaryNumber != 0) {
          decimalNumber += (binaryNumber % 10) * Math.pow(2, i);
          ++i;
          binaryNumber /= 10;
        }
    
        i = 1;
    
        while (decimalNumber != 0) {
          octalNumber += (decimalNumber % 8) * i;
          decimalNumber /= 8;
          i *= 10;
        }
    
        return octalNumber;
      }
}

