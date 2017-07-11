package main;

/**
 * Created by jipengqiang on 17/7/11.
 */

import Common.NMI;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.HashMap;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.HashMap;
import Common.NMI;


public class PYPM
{
    int K;
    double alpha;
    double beta;
    int iterNum;
    String dataset;

    HashMap<String, Integer> wordToIdMap;
    int V;
    DocumentSet documentSet;
    String dataDir = "data/";
    String outputPath = "result/";
    int []clusterRes;


    public PYPM(int K, double alpha, double beta, int iterNum, String dataset)
    {
        this.K = K;
        this.alpha = alpha;
        this.beta = beta;
        this.iterNum = iterNum;
        this.dataset = dataset;
        this.wordToIdMap = new HashMap<String, Integer>();
    }

    public void mainFun() throws Exception
    {
        long startTime = System.currentTimeMillis();
        getDocuments();
        long endTime = System.currentTimeMillis();
        System.out.println("getDocuments Time Used:" + (endTime-startTime)/1000.0 + "s");


        startTime = System.currentTimeMillis();
        double nmi = 0.0;
        int times =1;
        for(int i=0; i<times; i++) {
            runPYPM();
            nmi += printNMI();
        }
        endTime = System.currentTimeMillis();
        System.out.println("gibbsSampling Time Used:" + (endTime-startTime)/1000.0 + "s");

        System.out.println(dataset + " average nmi: " + nmi/times);
    }

    public void mainFunPrintLabel() throws Exception
    {
        String resPath = "SResult/FGSDMM/";



        long startTime = System.currentTimeMillis();
        getDocuments();
        long endTime = System.currentTimeMillis();
        System.out.println("getDocuments Time Used:" + (endTime-startTime)/1000.0 + "s");


        startTime = System.currentTimeMillis();
        double nmi = 0.0;
        int times =5;
        for(int i=0; i<times; i++) {
            String textName = resPath + "iter_" + i+".txt";
            runPYPM();
            //printLabel(textName);
            nmi += printNMI();
        }
        endTime = System.currentTimeMillis();
        System.out.println("gibbsSampling Time Used:" + (endTime-startTime)/(1000.0*times) + "s");

        System.out.println(dataset + " average nmi: " + nmi/times);
    }


    public static void main(String args[]) throws Exception
    {
        int K = 500;
        double alpha = 1;
        double beta = 0.05;
        int iterNum = 50;
        String dataset = "TS";
        PYPM fgsdmm = new PYPM(K, alpha, beta, iterNum, dataset);

        fgsdmm.mainFunPrintLabel();
        // fgsdmm.printLabel();
        //fgsdmm.mainFun();
    }

    public void getDocuments() throws Exception
    {
        documentSet = new DocumentSet(dataDir + dataset, wordToIdMap);
        V = wordToIdMap.size();
        //clusterRes = new int[documentSet.D];
    }

    public void runPYPM() throws Exception
    {
        String ParametersStr = "K"+K+"iterNum"+ iterNum +"alpha" + String.format("%.3f", alpha)
                + "beta" + String.format("%.3f", beta);
        FCluster model = new FCluster(K, V, iterNum,alpha, beta, dataset,  ParametersStr);
        //model.intialize(documentSet);
        model.intializeIndepent(documentSet);
        model.gibbsSampling(documentSet);
        //model.output(documentSet, outputPath);
        clusterRes = model.getClusterRes();
    }

    public void printLabel(String name)
    {
        try
        {
            FileWriter fw = new FileWriter(name);
            BufferedWriter bw = new BufferedWriter(fw);
            for(int i=0; i<clusterRes.length; i++)
                bw.write(String.valueOf(clusterRes[i])+ " ");

            bw.close();
            fw.close();
        }catch(Exception e)
        {
            e.printStackTrace();
        }

    }

    public  double printNMI()
    {
        System.out.println("GSDMM NMI value:");
        NMI nmi = new NMI();

        double nmiV = nmi.computNMI2(documentSet.getLabelsArr(),clusterRes);
        System.out.println(nmiV);
        return nmiV;
    }

}

