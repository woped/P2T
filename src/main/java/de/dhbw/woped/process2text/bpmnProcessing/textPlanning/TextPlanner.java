package de.dhbw.woped.process2text.bpmnProcessing.textPlanning;


import de.dhbw.woped.process2text.bpmnProcessing.contentDetermination.extraction.GatewayExtractor;
import de.dhbw.woped.process2text.bpmnProcessing.contentDetermination.labelAnalysis.EnglishLabelDeriver;
import de.dhbw.woped.process2text.bpmnProcessing.contentDetermination.labelAnalysis.EnglishLabelHelper;
import de.dhbw.woped.process2text.bpmnProcessing.dataModel.Pair;
import de.dhbw.woped.process2text.bpmnProcessing.dataModel.dsynt.DSynTConditionSentence;
import de.dhbw.woped.process2text.bpmnProcessing.dataModel.dsynt.DSynTMainSentence;
import de.dhbw.woped.process2text.bpmnProcessing.dataModel.dsynt.DSynTSentence;
import de.dhbw.woped.process2text.bpmnProcessing.dataModel.intermediate.AbstractFragment;
import de.dhbw.woped.process2text.bpmnProcessing.dataModel.intermediate.ConditionFragment;
import de.dhbw.woped.process2text.bpmnProcessing.dataModel.intermediate.ExecutableFragment;
import de.dhbw.woped.process2text.bpmnProcessing.dataModel.process.*;
import de.hpi.bpt.graph.algo.rpst.RPST;
import de.hpi.bpt.graph.algo.rpst.RPSTNode;
import de.hpi.bpt.process.ControlFlow;
import de.hpi.bpt.process.Node;
import de.hpi.bpt.process.Process;
import net.didion.jwnl.JWNLException;
import de.dhbw.woped.process2text.bpmnProcessing.preprocessing.FormatConverter;
import de.dhbw.woped.process2text.bpmnProcessing.templates.TemplateLoader;
import de.dhbw.woped.process2text.bpmnProcessing.textPlanning.recordClasses.ConverterRecord;
import de.dhbw.woped.process2text.bpmnProcessing.textPlanning.recordClasses.GatewayPropertyRecord;
import de.dhbw.woped.process2text.bpmnProcessing.textPlanning.recordClasses.ModifierRecord;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class TextPlanner {
	
	private RPST<ControlFlow,Node> rpst;
	private ProcessModel process;
	private TextToIntermediateConverter textToIMConverter;
	private ArrayList <ConditionFragment> passedFragments;
	private ModifierRecord passedMod = null; // used for AND-Splits
	private ArrayList<ModifierRecord> passedMods; // used for Skips 

	private boolean tagWithBullet = false;
	private boolean start = true;
	private boolean end = false;
	private boolean isAlternative = false;
	private int isolatedXORCount = 0;
	
	private ArrayList<DSynTSentence> sentencePlan;
	private ArrayList<Pair<Integer,DSynTSentence>> activitiySentenceMap;
	private EnglishLabelHelper lHelper;
	private EnglishLabelDeriver lDeriver;
	
	static String[] quantifiers = {"a", "the", "all", "any", "more", "most", "none", "some", "such", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten"};
	
	private boolean imperative;
	private String imperativeRole;
	
	public TextPlanner(RPST<ControlFlow,Node> rpst, ProcessModel process, EnglishLabelDeriver lDeriver, EnglishLabelHelper lHelper, String imperativeRole, boolean imperative, boolean isAlternative) throws FileNotFoundException, JWNLException {
		this.rpst = rpst;
		this.process = process;
		this.lHelper = lHelper;
		this.lDeriver = lDeriver;
		textToIMConverter = new TextToIntermediateConverter(rpst, process, lHelper, imperativeRole, imperative);
		passedFragments = new ArrayList<ConditionFragment>();
		sentencePlan = new ArrayList<DSynTSentence>();
		activitiySentenceMap = new ArrayList<Pair<Integer,DSynTSentence>>();
		passedMods = new ArrayList<ModifierRecord>();
		this.imperative = imperative;
		this.imperativeRole = imperativeRole;
		this.isAlternative = isAlternative;
	}
	
	/**
	 * Text Planning Main 
	 * @throws FileNotFoundException 
	 */
	public void convertToText(RPSTNode<ControlFlow, Node> root, int level) throws JWNLException, FileNotFoundException {
		// Order nodes of current level with respect to control flow
		ArrayList<RPSTNode<ControlFlow, Node>> orderedTopNodes = PlanningHelper.sortTreeLevel(root, root.getEntry(), rpst);
		// For each node of current level
		for (RPSTNode<ControlFlow,Node> node: orderedTopNodes) {
			
			// If we face an end event
			if ((PlanningHelper.isEvent(node.getExit()) && orderedTopNodes.indexOf(node) == orderedTopNodes.size()-1)) {
				end = true;
			} else {
				end = false;
			}
			
			int depth = PlanningHelper.getDepth(node, rpst);
			
			
			if (PlanningHelper.isBond(node)) {
				// Converter Record
				ConverterRecord convRecord = null;

				//**************************************  LOOP - SPLIT  **************************************
				if (PlanningHelper.isLoop(node,rpst)) {
					convRecord = getLoopConverterRecord(node);
				}	
				//**************************************  SKIP - SPLIT  **************************************
				if (PlanningHelper.isSkip(node,rpst)) {
					convRecord = getSkipConverterRecord(orderedTopNodes, node);
				}	
				//**************************************  XOR - SPLIT  **************************************
				if (PlanningHelper.isXORSplit(node, rpst)) {
					convRecord = getXORConverterRecord(node);
				}
				//**************************************  EVENT BASED - SPLIT  **************************************
				if (PlanningHelper.isEventSplit(node, rpst)) {
					convRecord = getXORConverterRecord(node);
				}
				//**************************************  OR - SPLIT  **************************************
				if (PlanningHelper.isORSplit(node, rpst)) {
					convRecord = getORConverterRecord(node);
				}
				//**************************************  AND - SPLIT  **************************************
				if (PlanningHelper.isANDSplit(node, rpst)) {
					convRecord = getANDConverterRecord(node);
				}	
				
				// Add pre statements 
				if (convRecord != null && convRecord.preStatements != null) {
					for (DSynTSentence preStatement: convRecord.preStatements) {
						if (passedFragments.size()>0) {
							if(tagWithBullet){
								preStatement.getExecutableFragment().sen_hasBullet = true;
								preStatement.getExecutableFragment().sen_level = level;
								passedFragments.get(0).sen_hasBullet = true;
								passedFragments.get(0).sen_level = level;
								tagWithBullet = false;
							}
							DSynTConditionSentence dsyntSentence = new DSynTConditionSentence(preStatement.getExecutableFragment(), passedFragments.get(0));
							if (passedFragments.size() > 1) {
								for (int i = 1; i < passedFragments.size(); i++) {
									dsyntSentence.addCondition(passedFragments.get(i), true);
									dsyntSentence.getConditionFragment().addCondition(passedFragments.get(i));
								}
							}
							passedFragments.clear();
							sentencePlan.add(dsyntSentence);
						} else {
							if(tagWithBullet){
								preStatement.getExecutableFragment().sen_hasBullet = true;
								preStatement.getExecutableFragment().sen_level = level;
								tagWithBullet = false;
							}
							preStatement.getExecutableFragment().sen_level = level;
							if (passedMods.size() > 0 ) {
								preStatement.getExecutableFragment().addMod(passedMods.get(0).getLemma(), passedMods.get(0));	
								preStatement.getExecutableFragment().sen_hasConnective = true;
								passedMods.clear();
							}
							sentencePlan.add(new DSynTMainSentence(preStatement.getExecutableFragment()));
						}
					}
				}
				
				// Pass precondition
				if (convRecord != null && convRecord.pre != null) {
					if (passedFragments.size() > 0) {
						if (passedFragments.get(0).getFragmentType() == AbstractFragment.TYPE_JOIN) {
							ExecutableFragment eFrag = new ExecutableFragment("continue", "process", "", "");
							eFrag.bo_isSubject = true;
							DSynTConditionSentence dsyntSentence = new DSynTConditionSentence(eFrag, passedFragments.get(0));
							sentencePlan.add(dsyntSentence);
							passedFragments.clear();
						}
					}
					passedFragments.add(convRecord.pre);
				}
				
				// ################ Convert to Text #################
				if (PlanningHelper.isLoop(node,rpst) || PlanningHelper.isSkip(node,rpst)) {
					convertToText(node, level);
				}
				if (PlanningHelper.isXORSplit(node,rpst) || PlanningHelper.isORSplit(node, rpst) || PlanningHelper.isEventSplit(node, rpst)) {
					ArrayList<RPSTNode<ControlFlow, Node>> paths = PlanningHelper.sortTreeLevel(node, node.getEntry(), rpst);
					for (RPSTNode<ControlFlow, Node> path: paths) {
						tagWithBullet = true;
						convertToText(path, level+1);
					}
				}
				if (PlanningHelper.isANDSplit(node,rpst)) {
					
					ArrayList<RPSTNode<ControlFlow, Node>> paths = PlanningHelper.sortTreeLevel(node, node.getEntry(), rpst);
					for (RPSTNode<ControlFlow, Node> path: paths) {
						tagWithBullet = true;
						convertToText(path, level+1);
					}
				}
				
				// Add post statement to sentence plan
				if (convRecord != null && convRecord.postStatements != null) {
					for (DSynTSentence postStatement: convRecord.postStatements) {
						postStatement.getExecutableFragment().sen_level = level;
						sentencePlan.add(postStatement);
					}
				}
					
				// Pass post fragment
				if (convRecord.post != null) {
					passedFragments.add(convRecord.post);
				}
			
			//**************************************  RIGIDS *******************************************
			} else if (PlanningHelper.isRigid(node)){
				
				ArrayList<Integer> validIDs = new ArrayList<Integer>();
				validIDs.addAll(process.getActivites().keySet());
				validIDs.addAll(process.getEvents().keySet());
				
				// Transforming RPST subtree to Petri Net
				ArrayList<ArrayList<String>> runSequences = PlanningHelper.getRunSequencesFromRPSTFragment(node, process);
				
				TemplateLoader loader = new TemplateLoader();
				loader.loadTemplate(TemplateLoader.RIGID);
				
				ExecutableFragment eFrag = new ExecutableFragment(loader.getAction(),loader.getAddition() , loader.getObject(),"");
				eFrag.bo_hasIndefArticle = true;
				eFrag.addAssociation(Integer.valueOf(node.getEntry().getId()));
				sentencePlan.add(new DSynTMainSentence(eFrag));
			
				loader.loadTemplate(TemplateLoader.RIGID_MAIN);
				eFrag = new ExecutableFragment(loader.getAction(),loader.getObject(), "",loader.getAddition());
				eFrag.sen_hasConnective = true;
				eFrag.bo_hasArticle = false;
				eFrag.add_hasArticle = false;
				eFrag.bo_isSubject = true;
				eFrag.sen_hasColon = true;
				sentencePlan.add(new DSynTMainSentence(eFrag));
				
				// Main run
				ArrayList<String> mainRun = runSequences.get(0);
				boolean first = true;
				for (String id: mainRun) {
					if (validIDs.contains(Integer.valueOf(id))) {
						convertRigidElement(Integer.valueOf(id), level, first);
						first = false;
					}
				}
				
				loader.loadTemplate(TemplateLoader.RIGID_DEV);
				eFrag = new ExecutableFragment(loader.getAction(),loader.getObject() , "",loader.getAddition());
				ModifierRecord modRecord = new ModifierRecord(ModifierRecord.TYPE_ADV, ModifierRecord.TARGET_VERB);
				modRecord.addAttribute("adv-type", "sentential");
				eFrag.addMod("However,", modRecord);
				eFrag.bo_hasArticle = true;
				eFrag.bo_isSubject = true;
				eFrag.sen_hasConnective = true;
				eFrag.addAssociation(Integer.valueOf(node.getEntry().getId()));
				sentencePlan.add(new DSynTMainSentence(eFrag));
				
				// Save ID of first rigid element
				String rigidStartID = node.getEntry().getId();
				
				// Deviation runs
				for (int i=1; i<runSequences.size();i++) {
					
					// Get run
					ArrayList<String> run = runSequences.get(i);
					String currentActivity = "";
					String previousActivity = "";
					String subsequentActivity = "";

					// Determine number of activities in run (if only one the sequence would be unclear to the reader)
					int activityCount = 0;
					for (String id: run) {
						if (validIDs.contains(Integer.valueOf(id))) {
							currentActivity = id;	
							activityCount++;
						}
					}
					
					// If activity count = 1, add statement or activity to clarify run
					if (activityCount == 1) {
						
						// Run is alternative start
						if (run.get(0) == rigidStartID) {
							convertRigidStartActivity(Integer.valueOf(currentActivity), level);
						
						// Run just contains single activity (middle of rigid)	
						} else {
							int startId = mainRun.indexOf(run.get(0));
							for (int j=startId; j>=0;j--) {
								if (j==0) {
									System.err.println("No Connection to main run.");
								}
								if (validIDs.contains(Integer.valueOf(mainRun.get(j)))) {
									previousActivity = mainRun.get(j);
									break;
								}
							}
							convertIsolatedRigidActivity(Integer.valueOf(currentActivity), Integer.valueOf(previousActivity), level);
						}
						
					
					} else if (activityCount == 0) {
						
						boolean foundPrev = false;
						boolean foundSub = false;
						
						// Try to find previous and subsequent activity in the runs
						for (ArrayList<String> r: runSequences) {
							
							// considered run contains predecessor
							if (r.contains(run.get(0)) && !foundPrev) {
								int startId = r.indexOf(run.get(0));
								for (int j=startId; j>=0;j--) {
									if (validIDs.contains(Integer.valueOf(r.get(j)))) {
										previousActivity = r.get(j);
										foundPrev = true;
										break;
									}
								}
							}
							
							// considered run contains predecessor
							if (r.contains(run.get(run.size()-1)) && !foundSub) {
								int endId = r.indexOf(run.get(run.size()-1));
								for (int j=endId; j<r.size();j++) {
									if (validIDs.contains(Integer.valueOf(r.get(j)))) {
										subsequentActivity = r.get(j);
										foundSub = true;
										break;
									}
								}
							}
						}
						if (previousActivity.equals("")) {
							convertRigidStartActivity(Integer.valueOf(subsequentActivity), level);
						} else if (subsequentActivity.equals("")) {
							convertRigidEndActivity(Integer.valueOf(previousActivity), level);
						} else {	
							convertIsolatedRigidActivity(Integer.valueOf(subsequentActivity), Integer.valueOf(previousActivity), level);
						}
						
						
					// Multiple activities: run is clear	
					} else {
						first = true;
						for (String id: run) {
							if (validIDs.contains(Integer.valueOf(id))) {
								if (first) {
									convertRigidStartActivity(Integer.valueOf(id), level);
									first = false;
								} else {
									convertRigidElement(Integer.valueOf(id), level, first);
								}
							}
						}
					}
				}
				
			//**************************************  ACTIVITIES  **************************************	
			} else if (PlanningHelper.isTask(node.getEntry())) {
				
				convertActivities(node, level, depth);
				
				// Handle End Event
				if (PlanningHelper.isEvent(node.getExit())) {
					end = false;
					Event event = process.getEvents().get((Integer.valueOf(node.getExit().getId())));
					if (event.getType() == EventType.END_EVENT && orderedTopNodes.indexOf(node) == orderedTopNodes.size()-1) {
						// Adjust level and add to sentence plan
						DSynTSentence sen = textToIMConverter.convertEvent(event).preStatements.get(0);
						sen.getExecutableFragment().sen_level = level;
						if (event.getSubProcessID() > 0) {
							sen.getExecutableFragment().sen_level = level+1;
						}
						sentencePlan.add(sen);
					}
				}
			} 
			//**************************************  EVENTS  **************************************	
//			 else if (PlanningHelper.isEvent(node.getEntry()) && orderedTopNodes.indexOf(node) > 0) {
			 else if (PlanningHelper.isEvent(node.getEntry())) {	 
				Event event = process.getEvents().get((Integer.valueOf(node.getEntry().getId())));
				int currentPosition = orderedTopNodes.indexOf(node);
				// Start Event
				if (currentPosition == 0) {
					
					// Start event should be printed
					if (start == true && isAlternative == false) {
						
						// Event is followed by gateway --> full sentence
						if (event.getType() == EventType.START_EVENT && currentPosition < orderedTopNodes.size()-1 && PlanningHelper.isBond(orderedTopNodes.get(currentPosition+1))) {
							start = false;
							ExecutableFragment eFrag = new ExecutableFragment("start", "process", "", "with a decision");
							eFrag.add_hasArticle = false;
							eFrag.bo_isSubject = true;
							sentencePlan.add(new DSynTMainSentence(eFrag));
						}
						if (event.getType() != EventType.START_EVENT) {
							start = false;
							ConverterRecord convRecord = textToIMConverter.convertEvent(event);
							if (convRecord != null && convRecord.hasPreStatements() == true) {
								sentencePlan.add(convRecord.preStatements.get(0));
							}
						}
					}
					
					
				// Intermediate Events	
				} else {
					ConverterRecord convRecord = textToIMConverter.convertEvent(event);
					
					// Add fragments if applicable
					if (convRecord != null && convRecord.pre != null) {
						passedFragments.add(convRecord.pre);
					}
					
					// Adjust level and add to sentence plan (first sentence not indented)
					if (convRecord != null && convRecord.hasPreStatements() == true) {
						for (int i = 0; i <convRecord.preStatements.size(); i++) {
							
							DSynTSentence sen = convRecord.preStatements.get(i);
							
							// If only one sentence (e.g. "Intermediate" End Event)
							if (convRecord.preStatements.size() == 1) {
								sen.getExecutableFragment().sen_level = level;
							}
							
							if (tagWithBullet == true) {
								sen.getExecutableFragment().sen_hasBullet = true;
								sen.getExecutableFragment().sen_level = level;
								tagWithBullet = false;
							}

							if (i>0) {
								sen.getExecutableFragment().sen_level = level;
							}
							if (event.getSubProcessID() > 0) {
								sen.getExecutableFragment().sen_level = level+1;
							}
							
							if (passedMods.size() > 0 ) {
								String mod = passedMods.get(0).getLemma();
								if (mod.equals("alternatively,") && sen.getExecutableFragment().sen_hasBullet) {
									passedMods.clear();
								} else {
									sen.getExecutableFragment().addMod(passedMods.get(0).getLemma(), passedMods.get(0));	
									sen.getExecutableFragment().sen_hasConnective = true;
									passedMods.clear();
								}
							}
							
							if (passedFragments.size() > 0 ) {
								DSynTConditionSentence dsyntSentence = new DSynTConditionSentence(sen.getExecutableFragment(), passedFragments.get(0));
								if (passedFragments.size() > 1) {
									for (i = 1; i < passedFragments.size(); i++) {
										dsyntSentence.addCondition(passedFragments.get(i), true);
										dsyntSentence.getConditionFragment().addCondition(passedFragments.get(i));
									}
								}
								sentencePlan.add(dsyntSentence);
								passedFragments.clear();
							} else {
								if (sen.getClass().toString().endsWith("DSynTConditionSentence")) {
									DSynTConditionSentence dsyntSentence = new DSynTConditionSentence(sen.getExecutableFragment(), ((DSynTConditionSentence) sen).getConditionFragment());
									sentencePlan.add(dsyntSentence);
								} else {
									DSynTMainSentence dsyntSentence = new DSynTMainSentence(sen.getExecutableFragment());
									sentencePlan.add(dsyntSentence);
								}
							}
						}
					}
				}
			} else {
				if (depth > 0) {
					convertToText(node, level);
				}
			}	
			if(end){
				Event event = process.getEvents().get((Integer.valueOf(node.getExit().getId())));
				DSynTSentence sen = textToIMConverter.convertEvent(event).preStatements.get(0);
				sen.getExecutableFragment().sen_level = level;
				sentencePlan.add(sen);
			}
		}
	}
	
	private void convertIsolatedRigidActivity(int id, int prevId, int level) {
		
		Activity currActivity = (Activity) process.getActivity(id);
		Activity prevActivity = (Activity) process.getActivity(prevId);
		Annotation currAnno = currActivity.getAnnotations().get(0);
		Annotation prevAnno = prevActivity.getAnnotations().get(0);
	
		
		ExecutableFragment eFrag = null;
		String modLemma = "after " + prevAnno.getActions().get(0) + "ing " + prevAnno.getBusinessObjects().get(0);
		ModifierRecord modRecord = new ModifierRecord(ModifierRecord.TYPE_ADV, ModifierRecord.TARGET_VERB);
		modRecord.addAttribute("adv-type", "sentential");
				
		eFrag = new ExecutableFragment("may", "also "  + currAnno.getActions().get(0) + " the " + currAnno.getBusinessObjects().get(0), "", "");
		String role = getRole(currActivity, eFrag);
		eFrag.setRole(role);
		eFrag.bo_hasArticle = false;
		eFrag.sen_hasBullet = true;
		eFrag.sen_level = level +1;
		eFrag.addMod(modLemma, modRecord);
		
		DSynTMainSentence dsyntSentence = new DSynTMainSentence(eFrag);
		sentencePlan.add(dsyntSentence);
	}
	
	private void convertRigidEndActivity(int id, int level) {
		Activity activity = (Activity) process.getActivity(id);
		if (activity != null) {
			Annotation anno1 = activity.getAnnotations().get(0);
			ExecutableFragment eFrag = null;
			eFrag = new ExecutableFragment("may", "also end with " + anno1.getActions().get(0) + "ing " + anno1.getBusinessObjects().get(0), "", anno1.getAddition());
			eFrag.addAssociation(activity.getId());
			String role = getRole(activity, eFrag);
			eFrag.setRole(role);
			eFrag.bo_hasArticle = false;
			eFrag.sen_hasBullet = true;
			eFrag.sen_level = level +1;
			
			DSynTMainSentence dsyntSentence = new DSynTMainSentence(eFrag);
			sentencePlan.add(dsyntSentence);
		} 
	}
	
	private void convertRigidStartActivity(int id, int level) {
		Activity activity = (Activity) process.getActivity(id);
		if (activity != null) {
			Annotation anno1 = activity.getAnnotations().get(0);
			
			ExecutableFragment eFrag = null;
			eFrag = new ExecutableFragment("may", "also begin with " + anno1.getActions().get(0) + "ing " + anno1.getBusinessObjects().get(0), "", anno1.getAddition());
			eFrag.addAssociation(activity.getId());
			String role = getRole(activity, eFrag);
			eFrag.setRole(role);
			eFrag.bo_hasArticle = false;
			eFrag.sen_hasBullet = true;
			eFrag.sen_level = level +1;
			
			DSynTMainSentence dsyntSentence = new DSynTMainSentence(eFrag);
			sentencePlan.add(dsyntSentence);
		} else {
		}
	}
	
	private void convertRigidElement(int id, int level, boolean first) {
		Activity activity = (Activity) process.getActivity(id);
		if (activity != null) {
			Annotation anno1 = activity.getAnnotations().get(0);
			
			ExecutableFragment eFrag = null;
			eFrag = new ExecutableFragment(anno1.getActions().get(0), anno1.getBusinessObjects().get(0), "", anno1.getAddition());
			eFrag.addAssociation(activity.getId());
			String role = getRole(activity, eFrag);
			eFrag.setRole(role);
			
			if (first) {
				eFrag.sen_hasBullet = true;
			}
			eFrag.sen_level = level +1;
			
			DSynTMainSentence dsyntSentence = new DSynTMainSentence(eFrag);
			sentencePlan.add(dsyntSentence);
		} else {
		}
	}
	
	private void convertActivities(RPSTNode<ControlFlow, Node> node, int level, int depth) throws JWNLException, FileNotFoundException {
		
		boolean planned = false;	
		
		Activity activity = (Activity) process.getActivity(Integer.parseInt(node.getEntry().getId()));
		Annotation anno = activity.getAnnotations().get(0);
		ExecutableFragment eFrag = null;
		
		ConditionFragment cFrag = null;
		
		// Start of the process
		if (start == true && isAlternative == false) {
			start = false;
			ModifierRecord modRecord = new ModifierRecord(ModifierRecord.TYPE_ADV, ModifierRecord.TARGET_VERB);
			modRecord.addAttribute("starting_point", "+");
			
			String bo = anno.getBusinessObjects().get(0);
			eFrag = new ExecutableFragment(anno.getActions().get(0), bo, "", anno.getAddition());
			
			eFrag.addAssociation(activity.getId());
			eFrag.addMod("the process begins when", modRecord);
			
			String role = getRole(activity, eFrag);
			eFrag.setRole(role);
			if (anno.getActions().size() == 2) {
				ExecutableFragment eFrag2 = null;
				if (anno.getBusinessObjects().size() == 2) {
					eFrag2 = new ExecutableFragment(anno.getActions().get(1), anno.getBusinessObjects().get(1), "", "");
					eFrag2.addAssociation(activity.getId());
				} else {
					eFrag2 = new ExecutableFragment(anno.getActions().get(1), "", "", "");
					eFrag2.addAssociation(activity.getId());
				}
				
				correctArticleSettings(eFrag2);
				eFrag.addSentence(eFrag2);
			}
			
			if (bo.endsWith("s") && lHelper.isNoun(bo.substring(0,bo.length()-1))) {
				eFrag.bo_hasArticle = true;
			} else {
				eFrag.bo_hasIndefArticle = true;
			}
			
			// If imperative mode
			if (imperative == true && imperativeRole.equals(role) == true) {
				eFrag.verb_isImperative = true;
				eFrag.role_isImperative = true;
			}
			correctArticleSettings(eFrag);
			DSynTMainSentence dsyntSentence = new DSynTMainSentence(eFrag);
			sentencePlan.add(dsyntSentence);
			activitiySentenceMap.add(new Pair<Integer,DSynTSentence>(Integer.valueOf(node.getEntry().getId()), dsyntSentence));
			planned = true;
		} 
		
		// Standard case
		eFrag = new ExecutableFragment(anno.getActions().get(0), anno.getBusinessObjects().get(0), "", anno.getAddition());
		eFrag.addAssociation(activity.getId());
		String role = getRole(activity, eFrag);
		eFrag.setRole(role);
		if (anno.getActions().size() == 2) {
			ExecutableFragment eFrag2 = null;
			if (anno.getBusinessObjects().size() == 2) {
				eFrag2 = new ExecutableFragment(anno.getActions().get(1), anno.getBusinessObjects().get(1), "", "");
				if (eFrag.verb_IsPassive == true) {
					if (anno.getBusinessObjects().get(0).equals("") == true) {
						eFrag2.verb_IsPassive = true;
						eFrag.setBo(eFrag2.getBo());
						eFrag2.setBo("");
						eFrag.bo_hasArticle = true;
					} else {
						eFrag2.verb_IsPassive = true;
						eFrag2.bo_isSubject = true;
					}
					
				}
			} else {
				eFrag2 = new ExecutableFragment(anno.getActions().get(1), "", "", "");
				if (eFrag.verb_IsPassive == true) {
					eFrag2.verb_IsPassive = true;
				}
			}
			
			correctArticleSettings(eFrag2);
			eFrag2.addAssociation(activity.getId());
			eFrag.addSentence(eFrag2);
		}
		
		eFrag.sen_level = level;
		if (imperative == true && imperativeRole.equals(role) == true) {
			correctArticleSettings(eFrag);	
			eFrag.verb_isImperative = true;
			eFrag.setRole("");
		}
		if (activity.getSubProcessID() > 0) {
			eFrag.sen_level = level+1;
		}
		
		// In case of passed modifications (NOT AND - Split) 
		if (passedMods.size() > 0 && planned == false) {
			correctArticleSettings(eFrag);	
			eFrag.addMod(passedMods.get(0).getLemma(), passedMods.get(0));	
			eFrag.sen_hasConnective = true;
			passedMods.clear();
		}
			
		// In case of passed modifications (e.g. AND - Split) 
		if (passedMod != null && planned == false){
			correctArticleSettings(eFrag);
			eFrag.addMod(passedMod.getLemma(), passedMod);	
			eFrag.sen_hasConnective = true;
			passedMod = null;
		}	
			
		if (tagWithBullet == true) {
			eFrag.sen_hasBullet = true;
			tagWithBullet = false;
		}
			
		// In case of passed fragments (General handling)
		if (passedFragments.size() > 0 && planned == false) {
			correctArticleSettings(eFrag);
			DSynTConditionSentence dsyntSentence = new DSynTConditionSentence(eFrag, passedFragments.get(0));
			if (passedFragments.size() > 1) {
				for (int i = 1; i < passedFragments.size(); i++) {
					dsyntSentence.addCondition(passedFragments.get(i), true);
					dsyntSentence.getConditionFragment().addCondition(passedFragments.get(i));
				}
			}
			sentencePlan.add(dsyntSentence);
			activitiySentenceMap.add(new Pair<Integer,DSynTSentence>(Integer.valueOf(node.getEntry().getId()), dsyntSentence));
			passedFragments.clear();
			planned = true;
		}
		
		if (planned == false) {
			correctArticleSettings(eFrag);
			DSynTMainSentence dsyntSentence = new DSynTMainSentence(eFrag);
			sentencePlan.add(dsyntSentence);
			activitiySentenceMap.add(new Pair<Integer,DSynTSentence>(Integer.valueOf(node.getEntry().getId()), dsyntSentence));
		}
		

		// If activity has attached Events
		if (activity.hasAttachedEvents()) {
			ArrayList<Integer>attachedEvents = activity.getAttachedEvents();
			HashMap<Integer,ProcessModel>alternativePaths = process.getAlternativePaths();
			for (Integer attEvent: attachedEvents) {
				if (alternativePaths.keySet().contains(attEvent)) {
					// Transform alternative
					ProcessModel alternative = alternativePaths.get(attEvent);
					alternative.annotateModel(0, lDeriver, lHelper);
				
					// Consider complexity of the process
					if (alternative.getElemAmount() <= 3) {
						alternative.getEvents().get(attEvent).setLeadsToEnd(true);	
					}
					
					FormatConverter rpstConverter = new FormatConverter();
					Process p =  rpstConverter.transformToRPSTFormat(alternative);
					RPST<ControlFlow,Node> rpst = new RPST<ControlFlow,Node>(p);
					TextPlanner converter = new TextPlanner(rpst, alternative, lDeriver,lHelper, imperativeRole, imperative, true);
					PlanningHelper.printTree(rpst.getRoot(), 0, rpst);
					converter.convertToText(rpst.getRoot(), level+1);
					ArrayList <DSynTSentence> subSentencePlan = converter.getSentencePlan();
					for (int i = 0; i <subSentencePlan.size(); i++) {
						DSynTSentence sen = subSentencePlan.get(i);
						if (i==0) {
							sen.getExecutableFragment().sen_level = level;
						}
						if (i==1) {
							sen.getExecutableFragment().sen_hasBullet = true;
						}
						sentencePlan.add(sen);
					}
					converter = null;
					
					// Print sentence for subsequent normal execution
					sentencePlan.add(textToIMConverter.getAttachedEventPostStatement(alternative.getEvents().get(attEvent)));
				}
			}
		}
		
		
		if (depth > 0) {
			convertToText(node, level);
		}
	}
	
	
	/**
	 * Get ConverterRecord for AND
	 */
	private ConverterRecord getANDConverterRecord(RPSTNode<ControlFlow, Node> node) {

		ArrayList<RPSTNode<ControlFlow, Node>> andNodes = PlanningHelper.sortTreeLevel(node, node.getEntry(), rpst);	
		
//		// Only General Case, no need for non-bulletin and-branches
		ConverterRecord rec = textToIMConverter.convertANDGeneral(node, andNodes.size(), null);
		return rec;
	}	
				
				
	/**
	 * Get ConverterRecord for OR
	 */
	private ConverterRecord getORConverterRecord(RPSTNode<ControlFlow, Node> node) {
		GatewayPropertyRecord orPropRec = new GatewayPropertyRecord(node, rpst, process);
		
		// Labeled Case
		if (orPropRec.isGatewayLabeled() == true)  {
			return null;
			
		// Unlabeled case
		} else {
			return textToIMConverter.convertORSimple(node, null, false);
		}	
	}
	
	/**
	 * Get ConverterRecord for XOR
	 */
	private ConverterRecord getXORConverterRecord(RPSTNode<ControlFlow, Node> node) {
		GatewayPropertyRecord propRec = new GatewayPropertyRecord(node, rpst, process);
		
		// Labeled Case with Yes/No - arcs and Max. Depth of 1
		if (propRec.isGatewayLabeled()== true && propRec.hasYNArcs() == true && propRec.getMaxPathDepth() == 1) {
			GatewayExtractor gwExtractor = new GatewayExtractor(node.getEntry(), lHelper);
			
			// Add sentence
			for (DSynTSentence s: textToIMConverter.convertXORSimple(node, gwExtractor)) {
				sentencePlan.add(s);
			}
			return null;
		// General case
		} else {
			return textToIMConverter.convertXORGeneral(node);
		}	
	}
	
	/**
	 * Get ConverterRecord for Loop
	 */
	private ConverterRecord getLoopConverterRecord(RPSTNode<ControlFlow, Node> node) {
		RPSTNode<ControlFlow, Node> firstNodeInLoop = PlanningHelper.getNextNode(node, rpst);
		return textToIMConverter.convertLoop(node,firstNodeInLoop);
	}
	
	/**
	 * Get ConverterRecord for Skip 
	 */
	private ConverterRecord getSkipConverterRecord(ArrayList<RPSTNode<ControlFlow, Node>> orderedTopNodes, RPSTNode<ControlFlow, Node> node) {
		GatewayPropertyRecord propRec = new GatewayPropertyRecord(node, rpst, process);
		
		// Yes-No Case 
		if (propRec.isGatewayLabeled() == true && propRec.hasYNArcs() == true) {
			
			// Yes-No Case which is directly leading to the end of the process
			if (isToEndSkip(orderedTopNodes, node) == true) {
				return textToIMConverter.convertSkipToEnd(node);

			// General Yes/No-Case	
			} else {
				return textToIMConverter.convertSkipGeneral(node);
			}
		
		// General unlabeled Skip
		} else {
			return textToIMConverter.convertSkipGeneralUnlabeled(node);
		}
	}
			
	/**
	 * Evaluate whether skip leads to an end 
	 */
	private boolean isToEndSkip(ArrayList<RPSTNode<ControlFlow, Node>> orderedTopNodes, RPSTNode<ControlFlow, Node> node) {
		int currentPosition = orderedTopNodes.indexOf(node);
		if (currentPosition < orderedTopNodes.size()-1) {
			Node potEndNode = orderedTopNodes.get(currentPosition+1).getExit();
			if (PlanningHelper.isEndEvent(potEndNode,process) == true) {
				return true;
			} 
		}
		return false;
	}
	
	
	/**
	 * Returns role of a fragment.  
	 */
	private String getRole(Activity a, AbstractFragment frag) {
		if (a.getLane() == null) {
			frag.verb_IsPassive = true;
			frag.bo_isSubject = true;
			if (frag.getBo().equals("")) {
				frag.setBo("it");
				frag.bo_hasArticle = false;
			}
			return "";
		}
		String role = a.getLane().getName();
		if (role.equals("")) {
			role = a.getPool().getName();
		}
		if (role.equals("")) {
			frag.verb_IsPassive = true;
			frag.bo_isSubject = true;
			if (frag.getBo().equals("")) {
				frag.setBo("it");
				frag.bo_hasArticle = false;
			}
		}
		return role;
	}
	
	/**
	 * Checks and corrects the article settings. 
	 */
	public void correctArticleSettings(AbstractFragment frag) {
		String bo = frag.getBo();
		if (bo.endsWith("s") && bo.endsWith("ss") == false && frag.bo_hasArticle == true && lHelper.isNoun(bo.substring(0, bo.length()-1))== true) {
			bo = bo.substring(0, bo.length()-1);
			frag.setBo(bo);
			frag.bo_isPlural = true;
		}
		if (bo.contains("&")) {
			frag.bo_isPlural = true;
		}
		if (frag.bo_hasArticle == true) {
			String[] boSplit = bo.split(" ");
			if (boSplit.length > 1) {
				if (Arrays.asList(quantifiers).contains(boSplit[0].toLowerCase())) {
					 frag.bo_hasArticle = false;
				}
			}
		}
		if (bo.equals("") && frag.bo_hasArticle) {
			frag.bo_hasArticle = false;
		}
		if (bo.startsWith("their") || bo.startsWith("a ") || bo.startsWith("for")) {
			frag.bo_hasArticle = false;
		}
		String[] splitAdd = frag.getAddition().split(" ");
		if (splitAdd.length > 3 && lHelper.isVerb(splitAdd[1]) && splitAdd[0].equals("on") == false) {
			frag.add_hasArticle = false;
		} else {
			frag.add_hasArticle = true;
		}
		
	}

	public ArrayList<Pair<Integer, DSynTSentence>> getActivitiySentenceMap() {
		return activitiySentenceMap;
	}
	
	public ArrayList<DSynTSentence> getSentencePlan() {
		return sentencePlan;
	}

}


