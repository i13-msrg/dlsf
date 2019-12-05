package dev.salis.dlsf.boot;

import akka.actor.typed.ActorSystem;
import dev.salis.dlsf.core.master.MasterConfig;
import dev.salis.dlsf.core.template.AbstractSimulationTemplate;
import dev.salis.dlsf.core.worker.WorkerConfig;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

/**
 * Convenience class used for bootstrapping DLSF applications.
 */
public class Boot {

  /** Main method to be used when bootstrapping an application on command line. */
  public static void main(String[] args) {
    final List<String> argList = Arrays.asList(args);
    NodeType nodeType = parseNodeType(argList);
    String nodeName = parseNodeName(argList);
    start(nodeType, nodeName);
  }

  /** Start application and find simulation templates on the classpath automatically. */
  public static void start() {
    final Map<String, AbstractSimulationTemplate> simulationTemplateMap = findSimulationTemplates();
    // print found simulation templates
    System.out.println("Found " + simulationTemplateMap.size() + " simulation template(s):");
    simulationTemplateMap.keySet().forEach(System.out::println);
    start(null, null, simulationTemplateMap);
  }

  /** Start application with custom type and name. Find simulation templates on the classpath automatically. */
  public static void start(NodeType nodeType, String nodeName) {
    final Map<String, AbstractSimulationTemplate> simulationTemplateMap = findSimulationTemplates();
    start(nodeType, nodeName, simulationTemplateMap);
  }

  /** Start application with custom type and name. Use provided simulation template list. */
  public static void start(
      NodeType nodeType, String nodeName, List<AbstractSimulationTemplate> simulationTemplates) {
    Map<String, AbstractSimulationTemplate> templateMap = new HashMap<>(simulationTemplates.size());
    for (AbstractSimulationTemplate simulationTemplate : simulationTemplates) {
      templateMap.put(simulationTemplate.getName(), simulationTemplate);
    }
    start(nodeType, nodeName, templateMap);
  }

  private static void start(
      NodeType nodeType,
      String nodeName,
      Map<String, AbstractSimulationTemplate> simulationTemplateMap) {
    StringBuilder sb = new StringBuilder();
    if (nodeType == null) {
      nodeType = NodeType.STANDALONE;
    }
    if (nodeName == null) {
      nodeName = UUID.randomUUID().toString().replaceAll("-", "");
    }
    // print found simulation templates
    sb.append("Starting DLSF with parameters:")
        .append(System.lineSeparator())
        .append("Type: ")
        .append(nodeType)
        .append(System.lineSeparator())
        .append("Name: ")
        .append(nodeName)
        .append(System.lineSeparator());
    sb.append("Simulations: ");

    simulationTemplateMap.keySet().forEach(name -> sb.append(name).append(", "));
    System.out.println(sb.toString());
    WorkerConfig workerConfig = null;
    if (NodeType.WORKER.equals(nodeType) || NodeType.STANDALONE.equals(nodeType)) {
      workerConfig = new WorkerConfig(nodeName, simulationTemplateMap);
    }
    MasterConfig masterConfig = null;
    if (NodeType.MASTER.equals(nodeType) || NodeType.STANDALONE.equals(nodeType)) {
      masterConfig = new MasterConfig(simulationTemplateMap);
    }
    ActorSystem.create(BootActor.createBehavior(masterConfig, workerConfig), "DLSFSystem");
  }

  private static Map<String, AbstractSimulationTemplate> findSimulationTemplates() {
    final Reflections reflections =
        new Reflections(
            new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage(""))
                .setScanners(new SubTypesScanner()));
    final Set<Class<? extends AbstractSimulationTemplate>> simulationTemplates =
        reflections.getSubTypesOf(AbstractSimulationTemplate.class);
    Map<String, AbstractSimulationTemplate> templateMap = new HashMap<>(simulationTemplates.size());
    for (Class<? extends AbstractSimulationTemplate> template : simulationTemplates) {
      try {
        final AbstractSimulationTemplate instance = template.getConstructor().newInstance();
        templateMap.put(instance.getName(), instance);
      } catch (NoSuchMethodException
          | InvocationTargetException
          | InstantiationException
          | IllegalAccessException e) {
        throw new BootException(
            "DSLT Boot could not instantiate " + template.getCanonicalName(), e);
      }
    }
    return templateMap;
  }

  private static NodeType parseNodeType(List<String> argList) {
    final int flagIndex = argList.lastIndexOf(ArgumentFlags.NODE_TYPE);
    if (flagIndex < 0) {
      // Default
      return null;
    }
    final int valueIndex = flagIndex + 1;
    if (valueIndex >= argList.size() - 1) {
      throw new BootException(ArgumentFlags.NODE_TYPE + " argument has no value");
    }
    final String valueStr = argList.get(valueIndex);
    try {
      return NodeType.valueOf(valueStr);
    } catch (IllegalArgumentException e) {
      throw new BootException(valueStr + "is not a valid node type", e);
    }
  }

  private static String parseNodeName(List<String> argList) {
    final int flagIndex = argList.lastIndexOf(ArgumentFlags.NODE_NAME);
    if (flagIndex < 0) {
      return null;
    }
    final int valueIndex = flagIndex + 1;
    if (valueIndex >= argList.size() - 1) {
      throw new BootException(ArgumentFlags.NODE_NAME + " argument has no value");
    }
    return argList.get(valueIndex);
  }
}
