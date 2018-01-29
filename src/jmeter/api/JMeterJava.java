package jmeter.api;

import java.io.File;
import java.io.FileOutputStream;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.control.gui.LoopControlPanel;
import org.apache.jmeter.control.gui.TestPlanGui;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.control.gui.HttpTestSampleGui;
import org.apache.jmeter.protocol.http.gui.HeaderPanel;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.threads.gui.ThreadGroupGui;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.testng.annotations.Test;


public class JMeterJava{

    @Test
    public void test() throws Exception {

        //Set jmeter home for the jmeter utils to load
        File jmeterHome = new File("C:\\Users\\200123\\Downloads\\apache-jmeter-3.3\\apache-jmeter-3.3");
        String slash = System.getProperty("file.separator");

        if (jmeterHome.exists()) {
            File jmeterProperties = new File(jmeterHome.getPath() + slash + "bin" + slash + "jmeter.properties");
            if (jmeterProperties.exists()) {
                //JMeter Engine
                StandardJMeterEngine jmeter = new StandardJMeterEngine();

                //JMeter initialization (properties, log levels, locale, etc)
                JMeterUtils.setJMeterHome(jmeterHome.getPath());
                JMeterUtils.loadJMeterProperties(jmeterProperties.getPath());
                JMeterUtils.initLogging();// you can comment this line out to see extra log messages of i.e. DEBUG level
                JMeterUtils.initLocale();

                // JMeter Test Plan, basically JOrphan HashTree
                HashTree testPlanTree = new HashTree();

                // First HTTP Sampler - open uttesh.com
                HTTPSamplerProxy httpSampler = new HTTPSamplerProxy();
                
                httpSampler.setDomain("10.252.1.41");
               
                
                //httpSampler.setDomain("localhost");
                httpSampler.setPort(8801);
                httpSampler.setPath("/oms/omsapi/v1/order/111");
                httpSampler.setMethod("GET");
                httpSampler.setName("OMS API");
                httpSampler.setProperty(TestElement.TEST_CLASS, HTTPSamplerProxy.class.getName());
                httpSampler.setProperty(TestElement.GUI_CLASS, HttpTestSampleGui.class.getName());

                //adding headers
                HeaderManager headerManager = new HeaderManager();
                headerManager.add(new Header("Authorization", "d2ViOnBhc3N3b3jk"));
                headerManager.add(new Header("x-cub-device", "crm"));
                headerManager.add(new Header("Content-Type", "application/json"));
                headerManager.add(new Header("x-cub-hdr", "{\"uid\":\"adsad-rqrr-weqeq-wqwq\",\"device\":\"web\"}"));
                headerManager.add(new Header("x-cub-uid", "11111111111"));
                headerManager.setName(JMeterUtils.getResString("header_manager_title"));
                headerManager.setProperty(TestElement.TEST_CLASS, HeaderManager.class.getName());
                headerManager.setProperty(TestElement.GUI_CLASS, HeaderPanel.class.getName());

                System.out.println("API Endpoint: " + httpSampler);
                System.out.println("HEADERS: " + headerManager.getHeaders());

                // Loop Controller
                LoopController loopController = new LoopController();
                loopController.setLoops(1);
                loopController.setFirst(true);
                loopController.setProperty(TestElement.TEST_CLASS, LoopController.class.getName());
                loopController.setProperty(TestElement.GUI_CLASS, LoopControlPanel.class.getName());
                loopController.initialize();

                // Thread Group
                ThreadGroup threadGroup = new ThreadGroup();
                threadGroup.setName("Sample Thread Group");
                threadGroup.setNumThreads(1);
                threadGroup.setRampUp(1);
                threadGroup.setSamplerController(loopController);
                threadGroup.setProperty(TestElement.TEST_CLASS, ThreadGroup.class.getName());
                threadGroup.setProperty(TestElement.GUI_CLASS, ThreadGroupGui.class.getName());

                // Test Plan
                TestPlan testPlan = new TestPlan("Create JMeter Script From Java Code");
                testPlan.setProperty(TestElement.TEST_CLASS, TestPlan.class.getName());
                testPlan.setProperty(TestElement.GUI_CLASS, TestPlanGui.class.getName());
                testPlan.setUserDefinedVariables((Arguments) new ArgumentsPanel().createTestElement());

                // HTTP Request Sampler and Header Manager
                HashTree requestHashTree = new HashTree();
                requestHashTree.add(httpSampler, headerManager);

                // Construct Test Plan from previously initialized elements
                testPlanTree.add(testPlan);
                HashTree threadGroupHashTree = testPlanTree.add(testPlan, threadGroup);
                threadGroupHashTree.add(requestHashTree);

                // save generated test plan to JMeter's .jmx file format
                SaveService.saveTree(testPlanTree, new FileOutputStream("report\\jmeter_api_sample.jmx"));

                //add Summarizer output to get test progress in stdout like:
                // summary =      2 in   1.3s =    1.5/s Avg:   631 Min:   290 Max:   973 Err:     0 (0.00%)
                Summariser summer = null;
               String summariserName = JMeterUtils.getPropDefault("summariser.name", "summary");
                if (summariserName.length() > 0) {
                    summer = new Summariser(summariserName);
                }

                // Store execution results into a .jtl file, we can save file as csv also
                String reportFile = "report\\report.jtl";
                String csvFile = "report\\report.csv";
                ResultCollector logger = new ResultCollector(summer);
                logger.setFilename(reportFile);
                ResultCollector csvlogger = new ResultCollector(summer);
                csvlogger.setFilename(csvFile);
                testPlanTree.add(testPlanTree.getArray()[0], logger);
                testPlanTree.add(testPlanTree.getArray()[0], csvlogger);

                // Run Test Plan
                jmeter.configure(testPlanTree);
                jmeter.run();

                System.out.println("Test completed. See " + jmeterHome + slash + "report.jtl file for results");
                System.out.println("JMeter .jmx script is available at " + jmeterHome + slash + "jmeter_api_sample.jmx");
                System.exit(0);

            }
        }

        System.err.println("jmeterHome property is not set or pointing to incorrect location");
        System.exit(1);

    }
}

