package de.dhbw.woped.process2text.bpmnProcessing.textPlanning;

import de.dhbw.woped.process2text.bpmnProcessing.dataModel.p2t.WFnet2Processes;
import de.dhbw.woped.process2text.bpmnProcessing.dataModel.petri.ProcessCover;
import de.dhbw.woped.process2text.bpmnProcessing.dataModel.process.*;
import de.dhbw.woped.process2text.bpmnProcessing.bpt.graph.algo.rpst.RPST;
import de.dhbw.woped.process2text.bpmnProcessing.bpt.graph.algo.rpst.RPSTNode;
import de.dhbw.woped.process2text.bpmnProcessing.bpt.process.ControlFlow;
import de.dhbw.woped.process2text.bpmnProcessing.bpt.process.Event;
import de.dhbw.woped.process2text.bpmnProcessing.bpt.process.Gateway;
import de.dhbw.woped.process2text.bpmnProcessing.bpt.process.Node;
import org.jbpt.bp.BehaviouralProfile;
import org.jbpt.bp.construct.BPCreatorNet;
import org.jbpt.petri.Flow;
import org.jbpt.petri.NetSystem;
import org.jbpt.petri.Place;
import org.jbpt.pm.FlowNode;
import org.jbpt.pm.structure.ProcessModel2NetSystem;
import org.jbpt.throwable.TransformationException;

import java.io.IOException;
import java.util.*;

public class PlanningHelper {

	/**
	 * Creates an order for the top level of a given RPST Tree.
	 */
	public static ArrayList<RPSTNode<ControlFlow, Node>> sortTreeLevel(
			RPSTNode<ControlFlow, Node> lnode, Node startElem,
			RPST<ControlFlow, Node> rpst) {

		if (PlanningHelper.isSplit(lnode, rpst)) {
			ArrayList<RPSTNode<ControlFlow, Node>> unordered = new ArrayList<RPSTNode<ControlFlow, Node>>();

			if (rpst.getChildren((lnode)).size() != 2) {
				unordered.addAll(rpst.getChildren((lnode)));
				return unordered;
			} else {
				unordered.addAll(rpst.getChildren((lnode)));
				if (getDepth(unordered.get(0), rpst) > getDepth(
						unordered.get(1), rpst)) {
					ArrayList<RPSTNode<ControlFlow, Node>> ordered = new ArrayList<RPSTNode<ControlFlow, Node>>();
					ordered.add(unordered.get(1));
					ordered.add(unordered.get(0));
					return ordered;
				} else {
					ArrayList<RPSTNode<ControlFlow, Node>> ordered = new ArrayList<RPSTNode<ControlFlow, Node>>();
					ordered.addAll(rpst.getChildren((lnode)));
					return ordered;
				}
			}
		}

		Collection<RPSTNode<ControlFlow, Node>> topNodes = rpst
				.getChildren((lnode));
		ArrayList<RPSTNode<ControlFlow, Node>> orderedTopNodes = new ArrayList<RPSTNode<ControlFlow, Node>>();

		if (isRigid(lnode)) {
			return orderedTopNodes;
		}

		Node currentElem = startElem;
		while (orderedTopNodes.size() < topNodes.size()) {
			for (RPSTNode<ControlFlow, Node> node : topNodes) {
				if (node.getEntry().equals(currentElem)) {
					orderedTopNodes.add(node);
					currentElem = node.getExit();
					break;
				}
			}
		}
		return orderedTopNodes;
	}

	/**
	 * Returns String representation of node.
	 */
	public static String getNodeRepresentation(Node n) {
		String s = "";
		if (PlanningHelper.isEvent(n)) {
			s = "Event + (" + n.getId() + ")";
		} else if (PlanningHelper.isGateway(n)) {
			Gateway g = (Gateway) n;
			if (g.isAND()) {
				s = "AND (" + n.getId() + ")";
			}
			if (g.isXOR()) {
				if (g.getName().equals("")) {
					s = "XOR (" + n.getId() + ")";
				}
				s = g.getName() + "(XOR," + n.getId() + ")";

			}
			if (g.isOR()) {
				if (g.getName().equals("")) {
					s = "OR (" + n.getId() + ")";
				}
				s = g.getName() + "(OR," + n.getId() + ")";

			}
		} else {
			s = n.toString();
		}
		return s;
	}

	/**
	 * Returns amount of nodes of the next level in the RPST.
	 */
	public static int getSubLevelCount(RPSTNode<ControlFlow, Node> node,
			RPST<ControlFlow, Node> rpst) {
		return rpst.getChildren(node).size();
	}

	/**
	 * Returns amount of nodes on the current RPST level.
	 */
	public static int getNodeCount(RPSTNode<ControlFlow, Node> node,
			RPST<ControlFlow, Node> rpst) {
		if (PlanningHelper.isTrivial(node)) {
			return 0;
		} else {
			Collection<RPSTNode<ControlFlow, Node>> children = rpst
					.getChildren(node);
			int sum = 0;
			for (RPSTNode<ControlFlow, Node> child : children) {
				sum = sum + 1 + getNodeCount(child, rpst);
			}
			return sum;
		}
	}

	/**
	 * Compute depth of a given component.
	 */
	public static int getDepth(RPSTNode<ControlFlow, Node> node,
			RPST<ControlFlow, Node> rpst) {
		int depth = getDepthHelper(node, rpst);
		if (depth > 1) {
			return depth - 1;
		} else {
			return depth;
		}
	}

	/**
	 * Helper for depth computation.
	 */
	public static int getDepthHelper(RPSTNode<ControlFlow, Node> node,
			RPST<ControlFlow, Node> rpst) {
		if (node.getName().startsWith("T")) {
			return 0;
		}
		ArrayList<Integer> depthValues = new ArrayList<Integer>();
		for (RPSTNode<ControlFlow, Node> n : rpst.getChildren(node)) {
			depthValues.add(getDepthHelper(n, rpst) + 1);
		}
		return Collections.max(depthValues);
	}

	/**
	 * Returns type of given bond.
	 */
	public static String getBondType(RPSTNode<ControlFlow, Node> bond,
			RPST<ControlFlow, Node> rpst) {
		if (isEventSplit(bond, rpst)) {
			return "EVENTBASED";
		}
		if (isANDSplit(bond, rpst)) {
			return "AND";
		}
		if (isXORSplit(bond, rpst)) {
			return "XOR";
		}
		if (isORSplit(bond, rpst)) {
			return "OR";
		}
		if (isSkip(bond, rpst)) {
			return "Skip";
		}
		if (isLoop(bond, rpst)) {
			return "Loop";
		}
		return "";
	}

	/**
	 * Decides whether a given bond is a loop (Arc from exit gateway to entry
	 * gateway).
	 */
	public static boolean isLoop(RPSTNode<ControlFlow, Node> bond,
			RPST<ControlFlow, Node> rpst) {
		if (isBond(bond) && isGateway(bond.getEntry())
				&& ((Gateway) bond.getEntry()).isXOR()) {
			for (RPSTNode<ControlFlow, Node> node : rpst.getChildren((bond))) {
				if (isTrivial(node) && node.getEntry().equals(bond.getExit())
						&& node.getExit().equals(bond.getEntry())
						&& isGateway(node.getExit())) {
					return true;
				}
				if (bond.getEntry().equals(node.getExit())
						&& isGateway(node.getExit())) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Decides whether a given bond is a skip (Arc from entry gateway to exit
	 * gateway).
	 */
	public static boolean isSkip(RPSTNode<ControlFlow, Node> bond,
			RPST<ControlFlow, Node> rpst) {
		if (isBond(bond) && isGateway(bond.getEntry())
				&& ((Gateway) bond.getEntry()).isXOR() && isSplit(bond, rpst)) {
			for (RPSTNode<ControlFlow, Node> node : rpst.getChildren((bond))) {
				if (isTrivial(node) && node.getEntry().equals(bond.getEntry())
						&& node.getExit().equals(bond.getExit())
						&& isGateway(node.getExit())) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Decides whether a given bond is a skip (All arcs are outgoing).
	 */
	public static boolean isSplit(RPSTNode<ControlFlow, Node> bond,
			RPST<ControlFlow, Node> rpst) {
		for (RPSTNode<ControlFlow, Node> node : rpst.getChildren((bond))) {
			if (node.getEntry() != bond.getEntry()) {
				return false;
			}
		}
		return true;
	}

	public static boolean isEventSplit(RPSTNode<ControlFlow, Node> bond,
			RPST<ControlFlow, Node> rpst) {
		if (isBond(bond) && isGateway(bond.getEntry())) {
			return ((Gateway) bond.getEntry()).isEventBased()
					&& isSplit(bond, rpst);
		}
		return false;
	}

	/**
	 * Decides whether a given bond is an AND split.
	 */
	public static boolean isANDSplit(RPSTNode<ControlFlow, Node> bond,
			RPST<ControlFlow, Node> rpst) {
		if (isBond(bond) && isGateway(bond.getEntry())) {
			return ((Gateway) bond.getEntry()).isAND() && isSplit(bond, rpst);
		}
		return false;
	}

	/**
	 * Decides whether a given gatewy is an XOR split.
	 */
	public static boolean isIsolatedXORSplit(RPSTNode<ControlFlow, Node> node,
			RPST<ControlFlow, Node> rpst) {
		if (isGateway(node.getEntry())) {
			return (((Gateway) node.getEntry()).isXOR()) && isSplit(node, rpst)
					&& !isSkip(node, rpst);
		}
		return false;
	}

	/**
	 * Decides whether a given bond is an XOR split.
	 */
	public static boolean isXORSplit(RPSTNode<ControlFlow, Node> bond,
			RPST<ControlFlow, Node> rpst) {
		if (isBond(bond) && isGateway(bond.getEntry())) {
			return (((Gateway) bond.getEntry()).isXOR()) && isSplit(bond, rpst)
					&& !isSkip(bond, rpst);
		}
		return false;
	}

	/**
	 * Decides whether a given bond is an OR split.
	 */
	public static boolean isORSplit(RPSTNode<ControlFlow, Node> bond,
			RPST<ControlFlow, Node> rpst) {
		if (isBond(bond) && isGateway(bond.getEntry())) {
			return ((Gateway) bond.getEntry()).isOR() && isSplit(bond, rpst);
		}
		return false;
	}

	/**
	 * Decides whether a given component is a Bond.
	 */
	public static boolean isBond(RPSTNode<ControlFlow, Node> node) {
		return node.getName().startsWith("B");
	}

	/**
	 * Decides whether a given component is a trivial one.
	 */
	public static boolean isTrivial(RPSTNode<ControlFlow, Node> node) {
		return node.getName().startsWith("T");
	}

	/**
	 * Decides whether a given component is a Rigid.
	 */
	public static boolean isRigid(RPSTNode<ControlFlow, Node> node) {
		return node.getName().startsWith("R");
	}

	/**
	 * Decides whether a given node is a gateway.
	 */
	public static boolean isGateway(Node node) {
		return node.getClass().toString()
				.equals("class de.hpi.bpt.process.Gateway");
	}

	/**
	 * Decides whether considered event is an end event
	 */
	public static boolean isEndEvent(Object o, ProcessModel process) {
		if (o.getClass().toString().equals("class de.hpi.bpt.process.Event") == true) {
			dataModel.process.Event event = process.getEvents().get(
					Integer.valueOf(((Event) o).getId()));
			if (event.getType() == EventType.END_EVENT) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Return true if o is an HPI event
	 */
	public static boolean isEvent(Object o) {
		return o.getClass().toString().equals("class de.hpi.bpt.process.Event");
	}

	/**
	 * Returns true if o is a HPI task
	 */
	public static boolean isTask(Object o) {
		return o.getClass().toString().equals("class de.hpi.bpt.process.Task");
	}

	/**
	 * Chekcs whether bond stays in the same lane.
	 */
	private boolean staysInLane(RPSTNode<ControlFlow, Node> bond, Lane lane,
			ProcessModel process, RPST<ControlFlow, Node> rpst) {
		ArrayList<RPSTNode<ControlFlow, Node>> orderedTopNodes = PlanningHelper
				.sortTreeLevel(bond, bond.getEntry(), rpst);
		for (RPSTNode<ControlFlow, Node> node : orderedTopNodes) {
			int depth = PlanningHelper.getDepth(node, rpst);
			if (depth == 0 && PlanningHelper.isTrivial(node)) {
				int id = Integer.valueOf(node.getEntry().getId());
				if (process.getActivites().containsKey(id)) {
					Lane currentLane = process.getActivites().get(id).getLane();
					if (currentLane.getName().equals(lane.getName()) == false) {
						return false;
					}
				}
			} else {
				boolean stays = staysInLane(node, lane, process, rpst);
				if (stays == false) {
					return false;
				}
			}
		}
		return true;
	}

	public static ArrayList<ArrayList<String>> getRunSequencesFromRPSTFragment(
			RPSTNode<ControlFlow, Node> node, ProcessModel process) {

		ArrayList<ArrayList<String>> runSequences = new ArrayList<ArrayList<String>>();

		org.jbpt.pm.ProcessModel pm = new org.jbpt.pm.ProcessModel();
		HashMap<Integer, FlowNode> elements = new HashMap<Integer, FlowNode>();
		HashMap<String, String> orignalMapping = new HashMap<String, String>();

		// Transform Rigid into jbpt process model
		for (Node n : node.getFragment().getVertices()) {
			orignalMapping.put(n.getId(), n.getName());
			if (process.getGateways().containsKey(Integer.valueOf(n.getId()))) {
				int type = process.getGateways()
						.get(Integer.valueOf(n.getId())).getType();

				if (type == GatewayType.AND) {
					org.jbpt.pm.AndGateway gw = new org.jbpt.pm.AndGateway(
							n.getId());
					gw.setId(n.getId());
					elements.put(Integer.valueOf(n.getId()), gw);
				}
				if (type == GatewayType.XOR) {
					org.jbpt.pm.XorGateway gw = new org.jbpt.pm.XorGateway(
							n.getId());
					gw.setId(n.getId());
					elements.put(Integer.valueOf(n.getId()), gw);
				}
			} else {
				org.jbpt.pm.Activity a = new org.jbpt.pm.Activity(n.getId());
				a.setId(n.getId());
				elements.put(Integer.valueOf(n.getId()), a);
			}
		}

		for (de.hpi.bpt.graph.abs.AbstractDirectedEdge arc : node.getFragment()
				.getEdges()) {
			int sID = Integer.valueOf(arc.getSource().getId());
			int tID = Integer.valueOf(arc.getTarget().getId());
			pm.addControlFlow(elements.get(sID), elements.get(tID));
		}

		NetSystem netSystem = null;
		try {
			netSystem = ProcessModel2NetSystem.transform(pm);
		} catch (TransformationException e1) {
			e1.printStackTrace();
		}
		netSystem.loadNaturalMarking();
		ProcessCover fps = new ProcessCover(netSystem);

		int c = 0;

		for (dataModel.petri.Process p : fps.getCorrectProcesses()) {
			c++;

			// Create NetSystem from PetriNet
			NetSystem ns = new NetSystem();
			for (Flow flow : p.getCausalNet().getEdges()) {
				ns.addFlow(flow.getSource(), flow.getTarget());
			}
			ns.loadNaturalMarking();

			// Save run (NetSystem) as png
			// dotSource = serializer.serialize(ns);
			// try {
			// IOUtils.invokeDOT("src", c+".png", dotSource);
			// } catch (IOException e) {
			// e.printStackTrace();
			// }

			// add single start node if run contains several
			int startNodes = 0;
			for (org.jbpt.petri.Node n : ns.getNodes()) {
				if (ns.getDirectPredecessors(n).size() == 0) {
					startNodes++;
				}
			}
			if (startNodes > 1) {
				org.jbpt.petri.Node newN = new Place();
				ns.addNode(newN);
				for (org.jbpt.petri.Node n : ns.getNodes()) {
					if (ns.getDirectPredecessors(n).size() == 0 && n != newN) {
						ns.addEdge(newN, n);
					}
				}
			}

			// Compute behavioural profile
			BehaviouralProfile<NetSystem, org.jbpt.petri.Node> bp = BPCreatorNet
					.getInstance().deriveRelationSet(ns);

			// Go through all nodes and add relevant elems
			ArrayList<org.jbpt.petri.Node> relevantNodes = new ArrayList<org.jbpt.petri.Node>();
			for (org.jbpt.petri.Node n : ns.getNodes()) {
				if (orignalMapping.containsKey(n.getName())) {
					String oldProcessID = n.getName();

					// Add relevant activities
					for (org.jbpt.pm.Activity a : pm.getActivities()) {
						if (a.getId().equals(oldProcessID)) {
							relevantNodes.add(n);
						}
					}
					// Add relevant gateways
					for (org.jbpt.pm.Gateway g : pm.getGateways()) {
						if (g.getId().equals(oldProcessID)) {
							relevantNodes.add(n);
						}
					}
				}
			}

			// Go through relevant nodes and build up run sequence (assumption:
			// only sequence, no concurrent branches)
			ArrayList<org.jbpt.petri.Node> runSequence = new ArrayList<org.jbpt.petri.Node>();
			for (org.jbpt.petri.Node relNode : relevantNodes) {

				// If list is empty
				if (runSequence.size() == 0) {
					runSequence.add(relNode);
				} else {

					// If element is not already part of the list
					if (!runSequence.contains(relNode)) {

						// Determine correct position of elem
						for (int i = 0; i < runSequence.size(); i++) {
							String rel = bp.getRelationForEntities(
									runSequence.get(i), relNode).toString();
							if (rel.equals("<-")) {
								runSequence.add(i, relNode);
								break;
							}
							if (i == (runSequence.size() - 1)) {
								runSequence.add(relNode);
								break;
							}
						}
					}
				}
			}

			// Convert to a list of IDs
			ArrayList<String> runSequenceIDs = new ArrayList<String>();
			for (int i = 0; i < runSequence.size(); i++) {
				runSequenceIDs.add(runSequence.get(i).getName());
			}
			runSequences.add(runSequenceIDs);
		}

		WFnet2Processes wf2pi = null;
		try {
			wf2pi = new WFnet2Processes(netSystem);
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (List<org.jbpt.petri.Node> l: wf2pi.getPetriNetPaths()) {
		
			// Go through all nodes and add relevant elems
			ArrayList<org.jbpt.petri.Node> relevantNodes = new ArrayList<org.jbpt.petri.Node>();
			for (org.jbpt.petri.Node n : l) {
				if (orignalMapping.containsKey(n.getName())) {
					String oldProcessID = n.getName();

					// Add relevant activities
					for (org.jbpt.pm.Activity a : pm.getActivities()) {
						if (a.getId().equals(oldProcessID)) {
							relevantNodes.add(n);
						}
					}
					// Add relevant gateways
					for (org.jbpt.pm.Gateway g : pm.getGateways()) {
						if (g.getId().equals(oldProcessID)) {
							relevantNodes.add(n);
						}
					}
				}
			}
		}

		return runSequences;
	}

	/**
	 * Prints Text Structure.
	 */
	public static void printTextStructure(RPSTNode<ControlFlow, Node> root,
			int level, ProcessModel process, RPST<ControlFlow, Node> rpst) {

		ArrayList<RPSTNode<ControlFlow, Node>> orderedTopNodes = PlanningHelper
				.sortTreeLevel(root, root.getEntry(), rpst);
		for (RPSTNode<ControlFlow, Node> node : orderedTopNodes) {
			int depth = PlanningHelper.getDepth(node, rpst);

			if (PlanningHelper.isBond(node)) {
				printIndent(level);
				if (PlanningHelper.isLoop(node, rpst)) {
					System.out.println("LOOP [");
					printTextStructure(node, level + 1, process, rpst);
					printIndent(level);
					System.out.println("] (LOOP) ");
				}
				if (PlanningHelper.isSkip(node, rpst)) {
					System.out.println("SKIP [");
					printTextStructure(node, level + 1, process, rpst);
					printIndent(level);
					System.out.println("] (SKIP) ");
				}
				if (PlanningHelper.isXORSplit(node, rpst)) {
					System.out.println("XOR [");
					printTextStructure(node, level + 1, process, rpst);
					printIndent(level);
					System.out.println("] (XOR) ");
				}
				if (PlanningHelper.isANDSplit(node, rpst)) {
					System.out.println("AND [");
					printTextStructure(node, level + 1, process, rpst);
					printIndent(level);
					System.out.println("] (AND) ");
				}
			} else {
				if (PlanningHelper.isTask(node.getEntry())) {
					printIndent(level);
					Activity activity = (Activity) process.getActivity(Integer
							.parseInt(node.getEntry().getId()));
					Annotation anno = activity.getAnnotations().get(0);
//					System.out.println(anno.getActions().get(0) + " "
//							+ anno.getBusinessObjects().get(0) + " "
//							+ anno.getAddition());
					if (depth > 0) {
						printTextStructure(node, level, process, rpst);
					}
				} else {
					if (depth > 0) {
						printTextStructure(node, level, process, rpst);
					}
				}
			}
		}
	}

	/**
	 * Prints intend on screen (standard out).
	 */
	private static void printIndent(int level) {
		for (int i = 0; i < level; i++) {
			System.out.print("\t");
		}
	}

	/**
	 * Return next activity.
	 */
	public static RPSTNode<ControlFlow, Node> getNextNode(
			RPSTNode<ControlFlow, Node> root, RPST<ControlFlow, Node> rpst) {
		ArrayList<RPSTNode<ControlFlow, Node>> orderedTopNodes = PlanningHelper
				.sortTreeLevel(root, root.getEntry(), rpst);
		for (RPSTNode<ControlFlow, Node> node : orderedTopNodes) {
			int depth = PlanningHelper.getDepth(node, rpst);
			if (depth == 0 && PlanningHelper.isTrivial(node)) {
				return node;
			} else {
				return getNextNode(node, rpst);
			}
		}
		return null;
	}

	/**
	 * Determines activity count in RPST.
	 */
	public static int getActivityCount(RPSTNode<ControlFlow, Node> root,
			RPST<ControlFlow, Node> rpst) {
		int c = 0;
		ArrayList<RPSTNode<ControlFlow, Node>> orderedTopNodes = PlanningHelper
				.sortTreeLevel(root, root.getEntry(), rpst);
		for (RPSTNode<ControlFlow, Node> node : orderedTopNodes) {
			int depth = PlanningHelper.getDepth(node, rpst);
			if (depth == 0 && (PlanningHelper.isTask(node.getEntry()))) {
				c++;
				;
			} else {
				c = c + getActivityCount(node, rpst);
			}
		}
		return c;
	}

	/**
	 * Print a given RPST Tree.
	 */
	public static void printTree(RPSTNode<ControlFlow, Node> root, int level,
			RPST<ControlFlow, Node> rpst) {
		ArrayList<RPSTNode<ControlFlow, Node>> orderedTopNodes = PlanningHelper
				.sortTreeLevel(root, root.getEntry(), rpst);
		for (RPSTNode<ControlFlow, Node> node : orderedTopNodes) {
			int depth = PlanningHelper.getDepth(node, rpst);
			for (int i = 0; i < level; i++) {
				System.out.print("\t");
			}

			// Determine type of node for presentation purposes
			String entryString = PlanningHelper.getNodeRepresentation(node
					.getEntry());
			String exitString = PlanningHelper.getNodeRepresentation(node
					.getExit());

			if (PlanningHelper.isBond(node)) {
				System.out.println(node.getName() + " ("
						+ PlanningHelper.getBondType(node, rpst) + "," + depth
						+ ", " + PlanningHelper.getSubLevelCount(node, rpst)
						+ ") [" + entryString + " --> " + exitString + "]");
			} else {
				System.out.println(node.getName() + " (" + depth + ", "
						+ PlanningHelper.getSubLevelCount(node, rpst) + ") ["
						+ entryString + " --> " + exitString + "]");
			}

			if (depth > 0) {
				printTree(node, level + 1, rpst);
			}
		}
	}

	public static boolean containsRigid(RPSTNode<ControlFlow, Node> root,
			int level, RPST<ControlFlow, Node> rpst) {
		ArrayList<RPSTNode<ControlFlow, Node>> orderedTopNodes = PlanningHelper
				.sortTreeLevel(root, root.getEntry(), rpst);
		for (RPSTNode<ControlFlow, Node> node : orderedTopNodes) {
			int depth = PlanningHelper.getDepth(node, rpst);
			if (isRigid(node)) {
				return true;
			}
			if (depth > 0) {
				printTree(node, level + 1, rpst);
			}
		}
		return false;
	}

}
