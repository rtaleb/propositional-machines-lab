/*
    A benchmark for propositional machines in BeepBeep 3
    Copyright (C) 2020 Laboratoire d'informatique formelle

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package propmanlab;

import ca.uqac.lif.json.JsonFalse;
import ca.uqac.lif.json.JsonTrue;
import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.LatexNamer;
import ca.uqac.lif.labpal.Region;
import ca.uqac.lif.labpal.TitleNamer;
import ca.uqac.lif.labpal.table.ExperimentTable;
import ca.uqac.lif.mtnp.plot.TwoDimensionalPlot.Axis;
import ca.uqac.lif.mtnp.plot.gnuplot.Scatterplot;
import ca.uqac.lif.mtnp.table.Composition;
import ca.uqac.lif.mtnp.table.ExpandAsColumns;
import ca.uqac.lif.mtnp.table.RenameColumns;
import ca.uqac.lif.mtnp.table.TransformedTable;
import propmanlab.macros.LabStats;
import propmanlab.scenarios.simple.SimpleScenario;
import propmanlab.scenarios.temperature.TemperatureThresholdScenario;

import static propmanlab.AccessControlledStreamExperiment.PROXY;
import static propmanlab.AccessControlledStreamExperiment.WITH_PROXY;
import static propmanlab.StreamExperiment.LENGTH;
import static propmanlab.StreamExperiment.MEMORY;
import static propmanlab.StreamExperiment.THROUGHPUT;
import static propmanlab.StreamExperiment.TIME;
import static propmanlab.scenarios.Scenario.SCENARIO;

@SuppressWarnings("unused")
public class MyLaboratory extends Laboratory
{
  /**
   * The maximum length of a trace
   */
  public static int MAX_TRACE_LENGTH = 10000;

  /**
   * The interval, in number of events, between updates of the experiment's
   * measurements
   */
  public static int s_eventStep = 10;

  /**
   * A nicknamer
   */
  public static transient LatexNamer s_nicknamer = new LatexNamer();

  /**
   * A title namer
   */
  public static transient TitleNamer s_titleNamer = new TitleNamer();

  /**
   * An experiment factory
   */
  public transient StreamExperimentFactory m_factory = new StreamExperimentFactory(this);

  @Override
  public void setup()
  {
    setTitle("Benchmark for propositional machines in BeepBeep");
    setDoi("TODO");
    setAuthor("Rania Taleb, Sylvain Hallé");
    
    // Factory setup: adding scenarios
    {
      m_factory.addScenario(SimpleScenario.NAME, new SimpleScenario(getRandom()));
      m_factory.addScenario(TemperatureThresholdScenario.NAME, new TemperatureThresholdScenario());
    }

    Region big_r = new Region();
    big_r.add(SCENARIO, SimpleScenario.NAME, TemperatureThresholdScenario.NAME);
    big_r.add(WITH_PROXY, JsonTrue.instance, JsonFalse.instance);

    // Impact of proxy
    {
      ExperimentTable t_comparison_tp = new ExperimentTable(SCENARIO, WITH_PROXY, THROUGHPUT);
      t_comparison_tp.setShowInList(false);
      add(t_comparison_tp);
      ExperimentTable t_comparison_mem = new ExperimentTable(SCENARIO, WITH_PROXY, MEMORY);
      t_comparison_mem.setShowInList(false);
      add(t_comparison_mem);
      for (Region r : big_r.all(SCENARIO))
      {
        Region r_with = new Region(r);
        r_with.add(WITH_PROXY, JsonTrue.instance);
        AccessControlledStreamExperiment e_with = m_factory.get(r_with);
        Region r_without = new Region(r);
        r_without.add(WITH_PROXY, JsonFalse.instance);
        AccessControlledStreamExperiment e_without = m_factory.get(r_without);
        if (e_with == null || e_without == null)
        {
          continue;
        }
        t_comparison_tp.add(e_with);
        t_comparison_tp.add(e_without);
        t_comparison_mem.add(e_with);
        t_comparison_mem.add(e_without);
        {
          // Impact on time
          ExperimentTable et = new ExperimentTable(WITH_PROXY, LENGTH, TIME);
          et.setShowInList(false);
          add(et);
          et.add(e_with);
          et.add(e_without);
          TransformedTable tt = new TransformedTable(new ExpandAsColumns(WITH_PROXY, TIME), et);
          tt.setTitle("Impact of proxy on throughput for " + r.getString(SCENARIO));
          s_nicknamer.setNickname(tt, r, "tTp", "");
          add(tt);
          Scatterplot plot = new Scatterplot(tt);
          plot.setTitle("Impact of proxy on throughput for " + r.getString(SCENARIO));
          plot.setCaption(Axis.X, LENGTH).setCaption(Axis.Y, TIME);
          s_nicknamer.setNickname(plot, r, "tTp", "");
          add(plot);
        }
        {
          // Impact on memory
          ExperimentTable et = new ExperimentTable(WITH_PROXY, LENGTH, MEMORY);
          et.setShowInList(false);
          add(et);
          et.add(e_with);
          et.add(e_without);
          TransformedTable tt = new TransformedTable(new ExpandAsColumns(WITH_PROXY, MEMORY), et);
          tt.setTitle("Impact of proxy on memory for " + r.getString(SCENARIO));
          s_nicknamer.setNickname(tt, r, "tMem", "");
          add(tt);
          Scatterplot plot = new Scatterplot(tt);
          plot.setTitle("Impact of proxy on memory for " + r.getString(SCENARIO));
          plot.setCaption(Axis.X, LENGTH).setCaption(Axis.Y, MEMORY);
          s_nicknamer.setNickname(plot, r, "pMem", "");
          add(plot);
        }
      }
      TransformedTable tt_comparison_tp = new TransformedTable(new ExpandAsColumns(WITH_PROXY, THROUGHPUT), t_comparison_tp);
      tt_comparison_tp.setTitle("Impact of proxy on throughput");
      tt_comparison_tp.setNickname("tImpactThroughput");
      add(tt_comparison_tp);
      TransformedTable tt_comparison_mem = new TransformedTable(new ExpandAsColumns(WITH_PROXY, MEMORY), t_comparison_mem);
      tt_comparison_mem.setTitle("Impact of proxy on memory");
      tt_comparison_mem.setNickname("tImpactMemory");
      add(tt_comparison_mem);
    }

    // Macros
    add(new LabStats(this));
  }

  /**
   * Initializes the lab
   * @param args Command line arguments
   */
  public static void main(String[] args)
  {
    // Nothing else to do here
    MyLaboratory.initialize(args, MyLaboratory.class);
  }
}
