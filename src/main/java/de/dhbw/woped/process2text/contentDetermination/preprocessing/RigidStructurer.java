package de.dhbw.woped.process2text.contentDetermination.preprocessing;

import de.hpi.bpt.process.Process;
import ee.ut.bpstruct2.Restructurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RigidStructurer {

  Logger logger = LoggerFactory.getLogger(RigidStructurer.class);

  public Process structureProcess(Process p) {
    int count = 0;
    for (de.hpi.bpt.process.Gateway gw : p.getGateways())
      if (gw.getName().isEmpty()) gw.setName("gw" + count++);

    Restructurer str = new Restructurer(p);

    if (str.perform()) {
      logger.info("Process successfully structured");
      return str.proc;
    } else {
      logger.info("WARNING: Process cannot be structured");
      return null;
    }
  }
}
