package org.fusesource.cloudmix.agent.snippet

import cloudmix.common.dto.AgentDetails
import mop.MopProcess
import resources.AgentResource
import scalautil.TextFormatting._

import _root_.com.sun.jersey.lift.Requests.uri
import _root_.com.sun.jersey.lift.ResourceBean
import _root_.java.util.TreeMap
import _root_.java.util.Map.Entry
import _root_.net.liftweb.util.Helpers._
import _root_.scala.xml._
import _root_.scala.collection.jcl.Conversions._

/**
 * Snippets for viewing agents
 *
 * @version $Revision : 1.1 $
 */

object Agents {
  def agentLink(agent: AgentDetails): String = {
    agentLink(agent.getId)
  }

  def agentLink(agentId: String): String = {
    uri("/agents/" + agentId)
  }

  def processLink(process: MopProcess): String = {
    processLink(process.getId)
  }

  def processLink(processId: String): String = {
    uri("/processes/" + processId)
  }

  def siteLink(agent: AgentDetails): NodeSeq = {
    val href = agent.getHref
    if (href != null)
      <a href={href} class='site'>
        {agent.getId}
      </a>
    else
      Text("")
  }

}


import Agents._

class Agents {
  /*
  def list(xhtml: Group): NodeSeq = {

    ResourceBean.get match {
      case agents: AgentsResource =>
        // TODO shame there's not a standard conversion to scala list for Collection
        // wonder if we can zap this in Scala 2.8?
        def agentList = new ArrayList[AgentDetails](agents.getAgents)

        agentList.flatMap {
          agent: AgentDetails =>
                  bind("agent", xhtml,
                    "name" -> Text(agent.getId),
                    "site" -> siteLink(agent),
                    AttrBindParam("link", Text(agentLink(agent)), "href"))}

      case _ =>
        <p> <b>Warning</b>No Agent resources found!</p>
    }
  }
  */

  def index(xhtml: Group): NodeSeq = {
    ResourceBean.get match {
      case agent: AgentResource =>
        val details = agent.details
        val systemProperties = new TreeMap[String, String](details.getSystemProperties)
        val processes = new TreeMap[String, MopProcess](agent.processes)

        println("agent href " + details.getHref)

        bind("agent", xhtml,
          "site" -> siteLink(details),
          "containerType" -> asText(details.getContainerType),
          "hostname" -> asText(details.getHostname),
          "maximumFeatures" -> asText("" + details.getMaximumFeatures),
          "name" -> asText(details.getName),
          "id" -> asText(details.getId),
          "os" -> asText(details.getOs),
          "pid" -> asText("" + details.getPid),
          "profile" -> asText(details.getProfile),

          "systemProperty" -> systemProperties.entrySet.flatMap {
            //case (key : String, value : String) =>
            case entry: Entry[_, _] =>
              bind("systemProperty", chooseTemplate("agent", "systemProperty", xhtml),
                "name" -> asText(entry.getKey),
                "value" -> asText(entry.getValue))
          }.toSeq,

          "process" -> processes.entrySet.flatMap {
            //case (key : String, value : String) =>
            case entry: Entry[_, _] =>
              val process: MopProcess = entry.getValue
              bind("process", chooseTemplate("agent", "process", xhtml),
                "id" -> asText(entry.getKey),
                "commandLine" -> asText(process.getCommandLine),
                AttrBindParam("link", Text(processLink(process)), "href"))
          }.toSeq
          )

      case _ =>
        <p>
          <b>Warning</b>
          No Agent resources found!</p>
    }
  }

}