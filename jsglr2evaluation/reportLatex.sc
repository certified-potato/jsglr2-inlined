import $ivy.`com.lihaoyi::ammonite-ops:1.8.1`, ammonite.ops._

import $file.common, common._, Args._

import $file.spoofax, spoofax._
import org.spoofax.jsglr2.JSGLR2Variant
import org.spoofax.jsglr2.integration.IntegrationVariant
import org.spoofax.jsglr2.integration.ParseTableVariant

def latexTableTestSets(implicit args: Args) = {
    val s = new StringBuilder()

    s.append("\\begin{table}[]\n")
    s.append("\\begin{tabular}{|l|l|l|l|l|}\n")
    s.append("\\hline\n")
    s.append("Language & Source & Files & Lines & Size (bytes) \\\\\n")
    s.append("\\hline\n")

    config.languages.foreach { language =>
        s.append("\\multirow{" + language.sources.size + "}{*}{" + language.id + "}\n")

        language.sources.zipWithIndex.foreach { case (source, index) =>
            val files = ls.rec! language.sourcesDir
            val lines = files | read.lines | (_.size) sum
            val size = files | stat | (_.size) sum

            s.append("  & " + source.id + " & " + files.size + " & " + lines + " & " + size + " \\\\ ")

            if (index == language.sources.size - 1)
                s.append("\\hline\n");
            else
                s.append("\\cline{2-5}\n")
        }
    }

    s.append("\\end{tabular}\n")
    s.append("\\end{table}\n")

    s.toString
}

def execMeasurements(implicit args: Args) = {
    println("LateX reporting...")
    
    mkdir! args.latexDir

    write.over(args.latexDir / "testsets.tex", latexTableTestSets)

    config.languages.zipWithIndex.foreach { case(language, index) =>
        println(" " + language.id)
    }
}

@main
def ini(args: String*) = withArgs(args :_ *)(execMeasurements(_))