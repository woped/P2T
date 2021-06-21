package de.dhbw.woped.process2text.bpmnProcessing;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.itextpdf.text.DocumentException;
import contentDetermination.labelAnalysis.EnglishLabelDeriver;
import contentDetermination.labelAnalysis.EnglishLabelHelper;
import dataModel.dsynt.DSynTSentence;
import dataModel.jsonReader.JSONReader;
import dataModel.jsonStructure.Doc;
import dataModel.process.ProcessModel;
import de.hpi.bpt.graph.algo.rpst.RPST;
import de.hpi.bpt.process.ControlFlow;
import de.hpi.bpt.process.Node;
import de.hpi.bpt.process.Process;
import net.didion.jwnl.JWNLException;
import preprocessing.FormatConverter;
import sentencePlanning.DiscourseMarker;
import sentencePlanning.ReferringExpressionGenerator;
import sentencePlanning.SentenceAggregator;
import sentenceRealization.SurfaceRealizer;
import textPlanning.TextPlanner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


public class Main {
	
	private static EnglishLabelHelper lHelper;
	private static EnglishLabelDeriver lDeriver;
	
	/**
	 * Main function. 
	 */
	public static void main(String[] args) throws Exception{

		String file = "BicycleManufacturing.json";
//		String file = "RigidTest.json";
		
		// Set up label parsing classes
		lHelper = new EnglishLabelHelper();
		lDeriver  = new EnglishLabelDeriver(lHelper);
		
		// Load and generate from JSON files in directory
		createFromFile(file);
	}
	
	/**
	 *  Function for generating text from a model. The according process model must be provided to the function.
	 */
	public static String toText(ProcessModel model, int counter) throws JWNLException, IOException, ClassNotFoundException, DocumentException {
		String imperativeRole = ""; 
		boolean imperative = false;
		
		// Annotate model
		model.annotateModel(0,lDeriver,lHelper);
		
		// Convert to RPST
		FormatConverter formatConverter = new FormatConverter();
		Process p = formatConverter.transformToRPSTFormat(model);
		RPST<ControlFlow,Node> rpst = new RPST<ControlFlow,Node>(p);
		
		// Convert to Text
		TextPlanner converter = new TextPlanner(rpst, model, lDeriver, lHelper, imperativeRole, imperative, false);
		converter.convertToText(rpst.getRoot(), 0);
		ArrayList <DSynTSentence> sentencePlan = converter.getSentencePlan();
		
		// Aggregation
		SentenceAggregator sentenceAggregator = new SentenceAggregator(lHelper);
		sentencePlan = sentenceAggregator.performRoleAggregation(sentencePlan, model);
		
		// Referring Expression
		ReferringExpressionGenerator refExpGenerator = new ReferringExpressionGenerator(lHelper);
		sentencePlan  = refExpGenerator.insertReferringExpressions(sentencePlan, model, false);
		
		// Discourse Marker 
		DiscourseMarker discourseMarker = new DiscourseMarker();
		sentencePlan = discourseMarker.insertSequenceConnectives(sentencePlan);

		// Realization
		SurfaceRealizer surfaceRealizer = new SurfaceRealizer();
		String surfaceText = surfaceRealizer.realizePlan(sentencePlan);
		
		// Cleaning
		if (imperative == true) {
			surfaceText = surfaceRealizer.cleanTextForImperativeStyle(surfaceText, imperativeRole, model.getLanes());
		}
		
		surfaceText = surfaceRealizer.postProcessText(surfaceText);
		return surfaceText;
	}
	
	/**
	 * Loads JSON files from directory and writes generated texts 
	 */
	private static void createFromFile(String file) throws JsonSyntaxException, IOException {
		
		JSONReader reader = new JSONReader();
		Gson gson = new Gson();
		int counter = 0;
		
		Doc modelDoc = gson.fromJson(reader.getJSONStringFromFile(file), Doc.class);
		if (modelDoc.getChildShapes() != null) {
			try {
				reader.init();
				reader.getIntermediateProcessFromFile(modelDoc);
				ProcessModel model = reader.getProcessModelFromIntermediate();
				
				// Multi Pool Model
				if (model.getPools().size() > 1) {
					long time = System.currentTimeMillis();
					System.out.println();
					System.out.print("The model contains "  + model.getPools().size() + " pools: ");
					int count = 0;
					for (String role: model.getPools()) {
						if (count > 0 && model.getPools().size() > 2) {
							System.out.print(", ");
						}
						if (count ==  model.getPools().size()-1) {
							System.out.print(" and ");
						}
						System.out.print(role + " (" + (count+1) + ")");
						count++;
					}
					
					HashMap<Integer,ProcessModel> newModels = model.getModelForEachPool();
					for (ProcessModel m: newModels.values()) {
						try {
							m.normalize();
							m.normalizeEndEvents();
						} catch (Exception e) {
							System.out.println("Error: Normalization impossible");
							e.printStackTrace();
						}
						String surfaceText = toText(m,counter);
						System.out.println(surfaceText.replaceAll(" process ", " " + m.getPools().get(0) + " process " ));
					}
				} else {
					try {
						model.normalize();
						model.normalizeEndEvents();
					} catch (Exception e) {
						System.out.println("Error: Normalization impossible");
						e.printStackTrace();
					}
					String surfaceText = toText(model,counter);
					System.out.println(surfaceText);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
