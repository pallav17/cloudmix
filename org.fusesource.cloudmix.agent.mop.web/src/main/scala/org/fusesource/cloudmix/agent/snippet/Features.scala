package org.fusesource.cloudmix.agent.snippet

import org.fusesource.cloudmix.agent.resources.{FeatureResource, FeaturesResource}
import org.fusesource.cloudmix.scalautil.TextFormatting._
import org.fusesource.cloudmix.scalautil.Collections._

import scala.xml._
import scala.collection.JavaConversions._
import org.fusesource.cloudmix.common.dto.{ResourceList, Resource}

/**
 * @version $Revision : 1.1 $
 */
class Features {
/*  def list(xhtml: Group): NodeSeq = {
    ResourceBean.get match {
      case resource: FeaturesResource =>
        val list = resource.resources

        bind("feature", xhtml,
          formatResources(xhtml, list))

      case _ =>
        <p>
          <b>Warning</b>
          No Directory resources found!</p>
    }
  }

  def index(xhtml: Group): NodeSeq = {
    ResourceBean.get match {
      case resource: FeatureResource =>
        val list = resource.resources

        bind("feature", xhtml,
          "name" -> resource.id,
          formatResources(xhtml, list))

      case _ =>
        <p>
          <b>Warning</b>
          No Directory resources found!</p>
    }
  }

  def formatResources(xhtml: Group, list: ResourceList) = {
    "resource" -> list.getResources.flatMap {
      case child: Resource =>
        bind("resource", chooseTemplate("feature", "resource", xhtml),
          "name" -> asText(child.getName),
          AttrBindParam("link", Text(child.getHref), "href"))
    }.toSeq
  }*/
}