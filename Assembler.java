import java.util.HashMap;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Assembler{
    private static HashMap<String, String> instructions = new HashMap<>();
    private static int location = 0;
    private static Scanner scanner;
    private static HashMap<String, String> labelDict = new HashMap<>();

    public static void main(String[] args) {
        instructions = initializeInstructions(instructions);
        scanner = initializeFile(scanner);
        
        firstPass(scanner);
        labelDict.forEach((key, value) -> System.out.println(key + " " + value));

    }

    public static void firstPass(Scanner scan){
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

    public static Scanner initializeFile(Scanner scanner){
        try {
            File file = new File("SOURCE.txt");
            scanner = new Scanner(file);
        }catch(FileNotFoundException e){
            e.printStackTrace();
            System.exit(1);
        }
        return scanner;
    }
    public static HashMap<String, String> initializeInstructions(HashMap<String, String> instructions){
        instructions.put("HLT", "000000");
        instructions.put("TRAP", "011000");
        instructions.put("LDR", "000001");
        instructions.put("STR", "000010");
        instructions.put("LDA", "000011");
        instructions.put("LDX", "010001");
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
    
        return instructions;
    }
}

