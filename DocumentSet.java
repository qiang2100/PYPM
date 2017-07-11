package main;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import org.json.JSONObject;

public class DocumentSet{
	int D = 0;



    ArrayList<Document> documents = new ArrayList<Document>();

	ArrayList<Integer> labelsArr = new ArrayList<Integer>();

	public DocumentSet(String dataDir, HashMap<String, Integer> wordToIdMap) 
			 					throws Exception
	{
		BufferedReader in = new BufferedReader(new FileReader(dataDir));
		String line;
		
		while((line=in.readLine()) != null){


			JSONObject obj = new JSONObject(line);
			String text = obj.getString("text");
			//System.out.println(text);
			int label = obj.getInt("cluster");
			//if(label>=5)
			//	continue;
			D++;
			Document document = new Document(text, wordToIdMap);
			documents.add(document);

            //int label = obj.getInt("cluster");
            labelsArr.add(label);

		}
		
		in.close();
	}

    public ArrayList<Document> getDocuments() {
        return documents;
    }

    public ArrayList<Integer> getLabelsArr() {
        return labelsArr;
    }
}
