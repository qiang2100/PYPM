package main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.*;

/**
 * Created by jipengqiang on 12/20/16.
 */
public class FCluster {

    DocumentSet documentSet;
    int K;
    double alpha;
    double beta;
    String dataset;
    String ParametersStr;
    int V;
    int D;
    int iterNum;
    double alpha0;
    double beta0;
    double smallDouble = 1e-150;
    double largeDouble = 1e150;
    double delta = 0.9;

    Map<Integer, Cluster> pointId2Cluster = new HashMap<>(); //pointId=>cluster

    public class Cluster{
        public ArrayList<Integer> docIndexList;

        int[] n_zv;
        int n_z;
        int m_z;

        public Cluster(int V) {
            docIndexList = new ArrayList<>();
            n_z = 0;
            n_zv = new int[V];
            m_z = 0;
        }

        public List<Integer> getDocList() {
            return docIndexList;
        }

        /**
         * Returns the number of points that are stored in the cluster.
         *
         * @return  Number of points in cluster
         */
        public int size() {
            return docIndexList.size();
        }


        /**
         * Adds a single point in the cluster.
         *
         *     The point that we wish to add in the cluster.
         */
        public void addPoint(int docIndex){
            //int nk= docIndexList.size();

            //update cluster clusterParameters
            Document document = documentSet.documents.get(docIndex);

            docIndexList.add(docIndex);
            m_z++;

            for(int w = 0; w < document.wordNum; w++){
                int wordNo = document.wordIdArray[w];
                int wordFre = document.wordFreArray[w];
                n_zv[wordNo] += wordFre;
                n_z += wordFre;
            }
        }

        /**
         * Removes a point from the cluster.
         *
         *    The point that we wish to remove from the cluster
         */
        public void removePoint(int docIndex)
        {
            int index = docIndexList.indexOf(docIndex);
            if(index==-1) {
                return;
            }

            docIndexList.remove(index);
            m_z--;
            Document document = documentSet.documents.get(docIndex);
            for(int w = 0; w < document.wordNum; w++){
                int wordNo = document.wordIdArray[w];
                int wordFre = document.wordFreArray[w];
                n_zv[wordNo] -= wordFre;
                n_z -= wordFre;
            }
        }


    }

    public List<Cluster> clusterList = new ArrayList<>();

    public FCluster(int K, int V, int iterNum, double alpha, double beta,
                 String dataset, String ParametersStr)
    {
        this.dataset = dataset;
        this.ParametersStr = ParametersStr;
        this.alpha = alpha;
        this.beta = beta;
        this.K = K;
        this.V = V;
        this.iterNum = iterNum;
        this.alpha0 = K * alpha;
        this.beta0 = V * beta;
    }

    public Cluster generateCluster()
    {
        return new Cluster(V);
    }

    public int createNewCluster() {
        int clusterId=clusterList.size(); //the ids start enumerating from 0

        //create new cluster
        Cluster c = generateCluster();

        //add the new cluster in our list
        clusterList.add(clusterId, c);

        return clusterId;
    }

    public void intializeIndepent(DocumentSet documentSet)
    {

        this.documentSet = documentSet;
        D = documentSet.D;
        this.K = D;

        System.out.println("the number of documents: " + D);
        for(int d = 0; d < D; d++){

            int clusterId=createNewCluster();
            clusterList.get(clusterId).addPoint(d);
            pointId2Cluster.put(d, clusterList.get(clusterId));

        }
        System.out.println("the cluster number after initiation: " + clusterList.size());
    }


    public void intializeRan(DocumentSet documentSet)
    {
        this.documentSet = documentSet;
        D = documentSet.D;

        ArrayList<Integer> intArr = new ArrayList<>();

        for(int i=0; i<D; i++)
            intArr.add(i);

        //System.out.println(intArr.toString());

        Collections.shuffle(intArr);

        if(D>0)
        {
            int clusterId=createNewCluster();
            clusterList.get(clusterId).addPoint(intArr.get(0));
            pointId2Cluster.put(intArr.get(0), clusterList.get(clusterId));
        }

        for(int ind = 1; ind < intArr.size(); ind++){

            int d = intArr.get(ind);
            Document document = documentSet.documents.get(d);

            int totalClusters = clusterList.size();

            int sampledClusterId = sampleCluster(d, document,clusterList.size());

            if(sampledClusterId==totalClusters) { //if new cluster
                int newClusterId=createNewCluster();
                clusterList.get(newClusterId).addPoint(d);
            }
            else {
                clusterList.get(sampledClusterId).addPoint(d);

            }
            pointId2Cluster.put(d,clusterList.get(sampledClusterId));
        }
        System.out.println("the cluster number after initiation: " + clusterList.size());
    }

    public void intialize(DocumentSet documentSet)
    {
        this.documentSet = documentSet;
        D = documentSet.D;

        if(D>0)
        {
            int clusterId=createNewCluster();
            clusterList.get(clusterId).addPoint(0);
            pointId2Cluster.put(0, clusterList.get(clusterId));
        }

        for(int d = 1; d < D; d++){

            Document document = documentSet.documents.get(d);

            int totalClusters = clusterList.size();

            int sampledClusterId = sampleCluster(d, document,clusterList.size());

            if(sampledClusterId==totalClusters) { //if new cluster
                int newClusterId=createNewCluster();
                clusterList.get(newClusterId).addPoint(d);
            }
            else {
                clusterList.get(sampledClusterId).addPoint(d);

            }
            pointId2Cluster.put(d,clusterList.get(sampledClusterId));
        }
        System.out.println("the cluster number after initiation: " + clusterList.size());
    }

    public void gibbsSampling(DocumentSet documentSet)
    {
        for(int i = 0; i < iterNum; i++){
            for(int d = 0; d < D; d++){
                Document document = documentSet.documents.get(d);
                Cluster clu = pointId2Cluster.get(d);
                clu.removePoint(d);

                //if empty cluster remove it
                if(clu.size()==0) {
                    clusterList.remove(clu);
                    pointId2Cluster.remove(d);
                }

                int totalClusters = clusterList.size();

                int sampledClusterId = sampleCluster(d, document,totalClusters);

                if(sampledClusterId==totalClusters) { //if new cluster
                    int newClusterId=createNewCluster();
                    clusterList.get(newClusterId).addPoint(d);
                }
                else {
                    clusterList.get(sampledClusterId).addPoint(d);

                }
                pointId2Cluster.put(d,clusterList.get(sampledClusterId));
            }
          //  System.out.print(clusterList.size() + ",");
        }
        //System.out.println();
    }

    private int sampleCluster(int d, Document document,int KNon)
    {
        double[] prob = new double[KNon+1];
        int[] overflowCount = new int[KNon+1];

        for(int k = 0; k < KNon; k++){
            prob[k] =(clusterList.get(k).m_z + alpha) ;
            double valueOfRule2 = 1.0;
            int i = 0;
            for(int w=0; w < document.wordNum; w++){
                int wordNo = document.wordIdArray[w];
                int wordFre = document.wordFreArray[w];
                for(int j = 0; j < wordFre; j++){
                    if(valueOfRule2 < smallDouble){
                        overflowCount[k]--;
                        valueOfRule2 *= largeDouble;
                    }
                    valueOfRule2 *= (clusterList.get(k).n_zv[wordNo] + beta + j)
                            / (clusterList.get(k).n_z + beta0 + i);
                    i++;
                }
            }
            prob[k] *= valueOfRule2;
        }

        prob[KNon] =  alpha*(K-KNon);
        double valueOfRule2 = 1.0;
        int i = 0;
        for(int w=0; w < document.wordNum; w++){
           // int wordNo = document.wordIdArray[w];
            int wordFre = document.wordFreArray[w];
            for(int j = 0; j < wordFre; j++){
                if(valueOfRule2 < smallDouble){
                    overflowCount[KNon]--;
                    valueOfRule2 *= largeDouble;
                }
                valueOfRule2 *= (beta + j)
                        / (beta0 + i);
                i++;
            }
        }
        prob[KNon] *= valueOfRule2;

        reComputeProbs(prob, overflowCount, KNon+1);

        for(int k = 1; k < KNon+1; k++){
            prob[k] += prob[k - 1];
        }
        double thred = Math.random() * prob[KNon];
        int kChoosed;
        for(kChoosed = 0; kChoosed < KNon+1; kChoosed++){
            if(thred < prob[kChoosed]){
                break;
            }
        }

        return kChoosed;
    }

    private void reComputeProbs(double[] prob, int[] overflowCount, int K)
    {
        int max = Integer.MIN_VALUE;
        for(int k = 0; k < K; k++){
            if(overflowCount[k] > max && prob[k] > 0){
                max = overflowCount[k];
            }
        }

        for(int k = 0; k < K; k++){
            if(prob[k] > 0){
                prob[k] = prob[k] * Math.pow(largeDouble, overflowCount[k] - max);
            }
        }
    }

    public void output(DocumentSet documentSet, String outputPath) throws Exception
    {
        String outputDir = outputPath + dataset + ParametersStr + "/";

        File file = new File(outputDir);
        if(!file.exists()){
            if(!file.mkdirs()){
                System.out.println("Failed to create directory:" + outputDir);
            }
        }

        outputClusteringResult(outputDir, documentSet);
    }

    public void outputClusteringResult(String outputDir, DocumentSet documentSet) throws Exception
    {
        String outputPath = outputDir + dataset + "FGSDMMResult.txt";
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter
                (new FileOutputStream(outputPath), "UTF-8"));
        for(int d = 0; d < documentSet.D; d++){
            Cluster clu = pointId2Cluster.get(d);
            writer.write(clusterList.indexOf(clu) + "\n");
        }
        writer.flush();
        writer.close();
    }

    public int[] getClusterRes()
    {
        int z[] = new int[documentSet.getDocuments().size()];
        for(int d = 0; d < documentSet.D; d++){
            Cluster clu = pointId2Cluster.get(d);
            z[d] = clusterList.indexOf(clu);
        }
        return z;
    }

}
