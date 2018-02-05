package depsolver;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class Package {
  private String name;
  private String version;
  private Integer size;
  private List<List<String>> depends = new ArrayList<>();
  private List<String> conflicts = new ArrayList<>();

  public String getName() { return name; }
  public String getVersion() { return version; }
  public Integer getSize() { return size; }
  public List<List<String>> getDepends() { return depends; }
  public List<String> getConflicts() { return conflicts; }
  public void setName(String name) { this.name = name; }
  public void setVersion(String version) { this.version = version; }
  public void setSize(Integer size) { this.size = size; }
  public void setDepends(List<List<String>> depends) { this.depends = depends; }
  public void setConflicts(List<String> conflicts) { this.conflicts = conflicts; }
}

public class Main {
  public static void main(String[] args) throws IOException {
    TypeReference<List<Package>> repoType = new TypeReference<List<Package>>() {};
    List<Package> repo = JSON.parseObject(readFile(args[0]), repoType);
    TypeReference<List<String>> strListType = new TypeReference<List<String>>() {};
    List<String> initial = JSON.parseObject(readFile(args[1]), strListType);
    List<String> constraints = JSON.parseObject(readFile(args[2]), strListType);

    // CHANGE CODE BELOW:
    // using repo, initial and constraints, compute a solution and print the answer
//    for (Package p : repo) {
//      System.out.printf("package %s version %s\n", p.getName(), p.getVersion());
//      for (List<String> clause : p.getDepends()) {
//        System.out.printf("  dep:");
//        for (String q : clause) {
//          System.out.printf(" %s", q);
//        }
//        System.out.printf("\n");
//      }
//    }

    
    ArrayList<String> commands = new ArrayList<String>();
    ArrayList<String> posError= new ArrayList<String>();
    ArrayList<String> blacklist= new ArrayList<String>();
    int j = 0;
    while ( j < constraints.size()) {
    	String p = constraints.get(j).substring(1);
    	boolean result = install(p, commands, posError, repo, initial, blacklist);
    	j++;
    }
    
    
    
    
    
  }
  
  
  public static boolean install(String p, List<String> commands, List<String> posError, List<Package> repo, List<String> initial, List<String> blacklist) {
	  String v = "";
	  String type = "";
	  if (p.contains(">=")) {
		   v = p.substring(p.indexOf(">=")+1);
		   type = ">=";
		   p = p.substring(0, p.indexOf(">=") );
	  } else if (p.contains("<=")) {
		   v = p.substring(p.indexOf("<=")+1);
		   type = "<=";
		   p = p.substring(0, p.indexOf("<=") );
	  } else if (p.contains("=")) {
		   v = p.substring(p.indexOf("=")+1);
		   type = "=";
		   p = p.substring(0, p.indexOf("=") );
	  } else {
		type = "any";
	  }
		 
	  
	  if (blacklist.contains(p + type + v)) {
	  		return false;
	  	}
		int i = 0;
		int end = 0;
		while (i < repo.size() && end != 3) {
			Package thePackage = repo.get(i);
			if (thePackage.getName().equals(p)){
				if (type == "=" ){
					if (v == thePackage.getVersion()) {
						if (install(thePackage.getName(), commands, posError, repo, initial, blacklist) == true) {
							commands.add("+" + thePackage.getName() + "=" + thePackage.getVersion());
							return true;
						} else {
							return false;
						}
					}
				} else if(type == ">=") {
					if (Integer.parseInt(v) >= Integer.parseInt(thePackage.getVersion()) ) {
						if (install(thePackage.getName(), commands, posError, repo, initial, blacklist) == true) {
							if (end == 0) {
								commands.add("+" + thePackage.getName() + "=" + thePackage.getVersion());
								end = 1;
							} else {
								posError.add("+" + repo.get(i).getName() + "=" + repo.get(i).getVersion());
								end = 2;
							}
						}
					}
				} else if (type == "<=") {
					
				} else if (type == "any") {
					
				}
			} else if ( end !=0) {
				end = 3;
			}
			i++;
			
		}
		
	  return false;
  }
  
  
  
  static String readFile(String filename) throws IOException {
    BufferedReader br = new BufferedReader(new FileReader(filename));
    StringBuilder sb = new StringBuilder();
    br.lines().forEach(line -> sb.append(line));
    return sb.toString();
  }
}
