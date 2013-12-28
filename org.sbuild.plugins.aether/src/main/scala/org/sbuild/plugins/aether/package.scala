package org.sbuild.plugins

package object aether {

  implicit def stringToDependency(string: String): Dependency = Dependency(string)

  implicit def stringSeqToDependencySeq(stringSeq: Seq[String]): Seq[Dependency] = stringSeq.map(Dependency(_))

  implicit def stringToExclude(string: String): Exclude = Exclude(string)

}