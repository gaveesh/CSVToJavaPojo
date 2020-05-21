import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import javassist.*;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;

import java.io.*;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


import org.jsonschema2pojo.DefaultGenerationConfig;
import org.jsonschema2pojo.GenerationConfig;
import org.jsonschema2pojo.Jackson2Annotator;
import org.jsonschema2pojo.SchemaGenerator;
import org.jsonschema2pojo.SchemaMapper;
import org.jsonschema2pojo.SchemaStore;
import org.jsonschema2pojo.SourceType;
import org.jsonschema2pojo.rules.RuleFactory;
import com.sun.codemodel.JCodeModel;
public class ApplicationMain {

    /*public static void main(String[] args) throws Exception {
        String[] fieldNames = null;
        Class<?> rowObjectClass = null;
        try(BufferedReader stream = new BufferedReader(new InputStreamReader(ApplicationMain.class.getResourceAsStream("file.csv")))) {
            while(true) {
                String line = stream.readLine();
                if(line == null) {
                    break;
                }
                if(line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                if(rowObjectClass == null) {
                    fieldNames = line.split(",");
                    rowObjectClass = buildCSVClass(fieldNames);
                } else {
                    String[] values = line.split(",");
                    Object rowObject = rowObjectClass.newInstance();
                    for (int i = 0; i < fieldNames.length; i++) {
                        Field f = rowObjectClass.getDeclaredField(fieldNames[i]);
                        f.setAccessible(true);
                        f.set(rowObject, values[i]);

                    }
                    System.out.println(reflectToString(rowObject));
                }
            }
        }
    }

    private static int counter = 0;
    public static Class<?> buildCSVClass(String[] fieldNames) throws CannotCompileException, NotFoundException {
        ClassPool pool = ClassPool.getDefault();
        CtClass result = pool.makeClass("CSV_CLASS$" + (counter++));
        ClassFile classFile = result.getClassFile();
        ConstPool constPool = classFile.getConstPool();
        classFile.setSuperclass(Object.class.getName());
        for (String fieldName : fieldNames) {
            CtField field = new CtField(ClassPool.getDefault().get(String.class.getName()), fieldName, result);
            result.addField(field);
        }
        classFile.setVersionToJava5();
        return result.toClass();
    }

    public static String reflectToString(Object value) throws IllegalAccessException {
        StringBuilder result = new StringBuilder(value.getClass().getName());
        result.append("@").append(System.identityHashCode(value)).append(" {");
        for (Field f : value.getClass().getDeclaredFields()) {
            f.setAccessible(true);
            result.append("\n\t").append(f.getName()).append(" = ").append(f.get(value)).append(", ");
        }
        result.delete(result.length()-2, result.length());
        return result.append("\n}").toString();
    }*/

    public static void main(String[] args) throws IOException {
        try (InputStream in = new FileInputStream("/Users/gaveesh/Downloads/sparktutorial/src/main/resources/file.csv");) {
            CSV csv = new CSV(true, ',', in );
            List< String > fieldNames = null;
            if (csv.hasNext()) fieldNames = new ArrayList< >(csv.next());
            List <Map< String, String >> list = new ArrayList < > ();
            while (csv.hasNext()) {
                List < String > x = csv.next();
                Map < String, String > obj = new LinkedHashMap< >();
                for (int i = 0; i < fieldNames.size(); i++) {
                    obj.put(fieldNames.get(i), x.get(i));
                }
                list.add(obj);
            }
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.writeValue(new File("/Users/gaveesh/Downloads/sparktutorial/src/main/resources/input.json"), list);
        }

        String packageName="convertedPojo";
        File inputJson= new File("/Users/gaveesh/Downloads/sparktutorial/src/main/resources/input.json");
        File outputPojoDirectory=new File("/Users/gaveesh/Downloads/sparktutorial/src/main/java/convertedPojo");
        outputPojoDirectory.mkdirs();
        try {
            new ApplicationMain().convert2JSON(inputJson.toURI().toURL(), outputPojoDirectory, packageName, inputJson.getName().replace(".json", ""));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            System.out.println("Encountered issue while converting to pojo: "+e.getMessage());
            e.printStackTrace();
        }

    }


    public void convert2JSON(URL inputJson, File outputPojoDirectory, String packageName, String className) throws IOException{
        JCodeModel codeModel = new JCodeModel();
        URL source = inputJson;
        GenerationConfig config = new DefaultGenerationConfig() {
            @Override
            public boolean isGenerateBuilders() { // set config option by overriding method
                return true;
            }
            public SourceType getSourceType(){
                return SourceType.JSON;
            }
        };
        SchemaMapper mapper = new SchemaMapper(new RuleFactory(config, new Jackson2Annotator(config), new SchemaStore()), new SchemaGenerator());
        mapper.generate(codeModel, className, packageName, source);
        codeModel.build(outputPojoDirectory);
    }
}