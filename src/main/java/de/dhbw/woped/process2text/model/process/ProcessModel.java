package de.dhbw.woped.process2text.model.process;

import de.dhbw.woped.process2text.service.content.determination.label_analysis.EnglishLabelDeriver;
import de.dhbw.woped.process2text.service.content.determination.label_analysis.EnglishLabelHelper;
import de.dhbw.woped.process2text.service.content.determination.label_analysis.EnglishLabelProperties;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessModel {

  Logger logger = LoggerFactory.getLogger(ProcessModel.class);
  private final HashMap<Integer, Arc> arcs;
  private final HashMap<Integer, Activity> activities;
  private final HashMap<Integer, Event> events;
  private final HashMap<Integer, Gateway> gateways;
  private final ArrayList<String> lanes;
  private final ArrayList<String> pools;
  private final HashMap<Integer, ProcessModel> alternativePaths;

  private static final String GLOSSARY = "glossary://";

  public ProcessModel() {
    arcs = new HashMap<>();
    activities = new HashMap<>();
    events = new HashMap<>();
    gateways = new HashMap<>();
    lanes = new ArrayList<>();
    pools = new ArrayList<>();
    alternativePaths = new HashMap<>();
  }

  public int getNewId() {
    int base = 0;

    for (int i : arcs.keySet()) {
      if (i > base) {
        base = i;
      }
    }
    for (int i : activities.keySet()) {
      if (i > base) {
        base = i;
      }
    }
    for (int i : gateways.keySet()) {
      if (i > base) {
        base = i;
      }
    }
    for (int i : events.keySet()) {
      if (i > base) {
        base = i;
      }
    }
    base++;
    return base;
  }

  public HashMap<Integer, ProcessModel> getAlternativePaths() {
    return alternativePaths;
  }

  public Element getElem(int id) {
    if (events.containsKey(id)) {
      return events.get(id);
    }
    if (gateways.containsKey(id)) {
      return gateways.get(id);
    }
    if (activities.containsKey(id)) {
      return activities.get(id);
    }
    return null;
  }

  public void annotateModel(EnglishLabelDeriver lDeriver, EnglishLabelHelper lHelper) {
    for (Activity a : activities.values()) {
      EnglishLabelProperties props = new EnglishLabelProperties();
      try {
        String label = a.getLabel().toLowerCase().replaceAll("\n", " ");

        label = label.replaceAll(" {2}", " ");

        if (label.contains(GLOSSARY)) {
          label = label.replace(GLOSSARY, "");
          label = label.substring(label.indexOf("/") + 1, label.length());
          label = label.replace(";;", "");
        }

        String[] labelSplit = label.split(" ");
        lDeriver.deriveFromVOS(a.getLabel(), labelSplit, props);
        Annotation anno = new Annotation();

        // No Conjunction label
        if (!props.hasConjunction()) {

          // If no verb-object label
          if (!lHelper.isVerb(labelSplit[0])) {
            anno.addAction("conduct");
            anno.addBusinessObjects(a.getLabel().toLowerCase());
            a.addAnnotation(anno);

            // If verb-object label
          } else {
            anno.addAction(props.getAction());
            String bo = props.getBusinessObject();
            if (bo.startsWith("the ")) {
              bo = bo.replace("the ", "");
            }
            if (bo.startsWith("an ")) {
              bo = bo.replace("an ", "");
            }
            anno.addBusinessObjects((bo));
            String add = props.getAdditionalInfo();
            String[] splitAdd = add.split(" ");
            if (splitAdd.length > 2 && splitAdd[1].equals("the")) {
              add = add.replace("the ", "");
            }
            anno.setAddition(add);
            a.addAnnotation(anno);
          }
          // Conjunction label
        } else {
          for (String action : props.getMultipleActions()) {
            anno.addAction(action);
          }
          for (String bo : props.getMultipleBOs()) {
            String temp = bo;
            if (temp.startsWith("the ")) {
              temp = temp.replace("the ", "");
            }
            if (temp.startsWith("an ")) {
              temp = temp.replace("an ", "");
            }
            anno.addBusinessObjects(temp);
          }
          anno.setAddition("");
          a.addAnnotation(anno);
        }
      } catch (Exception e) {
        logger.error(e.getLocalizedMessage());
      }
    }
  }

  public int getElemAmount() {
    return activities.size() + gateways.size() + events.size();
  }

  public void addPool(String pool) {
    String temp = pool;
    if (temp.contains(GLOSSARY)) {
      temp = pool.replace(GLOSSARY, "");
      temp = temp.substring(temp.indexOf("/") + 1, temp.length());
      temp = temp.replace(";;", "");
    }
    pools.add(temp);
  }

  public void addArc(Arc arc) {
    arcs.put(arc.getId(), arc);
  }

  public void addActivity(Activity activity) {
    activities.put(activity.getId(), activity);
  }

  public void addEvent(Event event) {
    events.put(event.getId(), event);
  }

  public void addGateway(Gateway gateway) {
    gateways.put(gateway.getId(), gateway);
  }

  public HashMap<Integer, Arc> getArcs() {
    return arcs;
  }

  public HashMap<Integer, Activity> getActivites() {
    return activities;
  }

  public HashMap<Integer, Event> getEvents() {
    return events;
  }

  public HashMap<Integer, Gateway> getGateways() {
    return gateways;
  }

  public Activity getActivity(int id) {
    return activities.get(id);
  }

  public void addLane(String lane) {
    String temp = lane;
    if (temp.contains(GLOSSARY)) {
      temp = lane.replace(GLOSSARY, "");
      temp = temp.substring(temp.indexOf("/") + 1, temp.length());
      temp = temp.replace(";;", "");
    }
    lanes.add(temp);
  }

  @Override
  public String toString() {
    return "ProcessModel{"
        + "arcs="
        + arcs
        + ", activities="
        + activities
        + ", events="
        + events
        + ", gateways="
        + gateways
        + ", lanes="
        + lanes
        + ", pools="
        + pools
        + ", alternativePaths="
        + alternativePaths
        + '}';
  }

  public void deleteArc(Arc arc) {
    for (Map.Entry entry : arcs.entrySet()) {
      Arc a = (Arc) entry.getValue();
      if (a == arc) {
        arcs.remove(entry.getKey());
        break;
      }
    }
  }
}
