package org.jboss.forge.plugin.gitignore;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.inject.Inject;

import org.jboss.forge.parser.java.util.Strings;
import org.jboss.forge.project.Project;
import org.jboss.forge.shell.Shell;
import org.jboss.forge.shell.completer.CommandCompleter;
import org.jboss.forge.shell.completer.CommandCompleterState;

public class GitIgnoreTemplateCompleter implements CommandCompleter
{

   @Inject
   private Project project;
   
   @Inject
   private Shell shell;

   @Override
   public void complete(CommandCompleterState state)
   {
      Queue<String> tokens = state.getTokens();
      String peek = tokens.peek();
      List<String> candidates = candidates(peek);
      if (!candidates.isEmpty())
      {
         if (!Strings.isNullOrEmpty(peek))
         {
            state.setIndex(state.getBuffer().lastIndexOf(peek));
         }
         state.getCandidates().addAll(candidates);
      }
   }
   
   private List<String> candidates(String start)
   {
      List<String> result = new LinkedList<String>();
      for (GitIgnoreGroup group : project.getFacet(GitIgnoreFacet.class).list())
      {
         for (String template : group.getTemplates())
         {
            if (Strings.isNullOrEmpty(start) || template.startsWith(start))
            {
               result.add(template);
            }
         }
      }
      return result;
   }

}
