package de.dhbw.woped.process2text.service.text.planning.recordClasses;

import de.dhbw.woped.process2text.service.text.planning.PlanningHelper;
import de.hpi.bpt.graph.algo.rpst.RPST;
import de.hpi.bpt.graph.algo.rpst.RPSTNode;
import de.hpi.bpt.process.ControlFlow;
import de.hpi.bpt.process.Node;
import java.util.ArrayList;

public class GatewayPropertyRecord {
  private final RPSTNode<ControlFlow, Node> node;
  private final RPST<ControlFlow, Node> rpst;
  private int maxPathDepth = 0;
  private int maxPathActivityNumber = 0;
  private boolean isGatewayLabeled = false;
  private boolean hasYNArcs = false;

  public GatewayPropertyRecord(RPSTNode<ControlFlow, Node> node, RPST<ControlFlow, Node> rpst) {
    this.node = node;
    this.rpst = rpst;
    setGatewayPropertyRecord();
  }

  /** Evaluates and determines according values for Gateway. */
  private void setGatewayPropertyRecord() {
    // maxPathDepth / maxPathActivityNumber
    ArrayList<RPSTNode<ControlFlow, Node>> paths =
        (ArrayList<RPSTNode<ControlFlow, Node>>) rpst.getChildren(node);
    for (RPSTNode<ControlFlow, Node> pnode : paths) {
      int depth = PlanningHelper.getDepth(pnode, rpst);
      int number = rpst.getChildren(pnode).size() - 1;
      if (depth > maxPathDepth) {
        maxPathDepth = depth;
      }
      if (number > maxPathActivityNumber) {
        maxPathActivityNumber = number;
      }
    }

    // Labeling
    isGatewayLabeled = !node.getEntry().getName().equals("");
    hasYNArcs = true;
  }

  public int getMaxPathDepth() {
    return maxPathDepth;
  }

  public boolean isGatewayLabeled() {
    return isGatewayLabeled;
  }

  public boolean hasYNArcs() {
    return hasYNArcs;
  }
}
