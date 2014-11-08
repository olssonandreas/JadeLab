

package prologjava;

import gnu.prolog.term.Term;
import gnu.prolog.vm.PrologException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author edwardblurock
 */
public class QueryWineDatabase {
    
    ExecuteGoal goal = new ExecuteGoal();
    
    /** Default constructor
     * 
     */
    public QueryWineDatabase() {}
    
    /** Constructor that reads predicates from the specified PROLOG file
     * 
     * @param prologdatabase the name of the PROLOG input file
     */
    public QueryWineDatabase(String prologdatabase) {
        goal.readInPredicates(prologdatabase);
    }
    
    /** Return a list of wine names with certain sugar
     * 
     * @param query is the query to ask prolog
     * @return ArrayList of strings with wine names
     * @throws PrologException
     * @throws Exception 
     */
    
        public ArrayList<String> queryProlog(String query) throws PrologException, Exception
    {
        try {
            
            /**
             * Build a query for a predicate database of query parameter 
             * "Output" is the variable that will hold the name of the wine
             * "sugar" is the constant
             */
           
            //String query = String.format("wine(Output,_,%s,_,_,_,_)", region);

            // Execute query
            SetOfPrologAssignments executeGoal = goal.executeGoal(query);

            // Check if the query was successful
            if (executeGoal.isSuccess()) {
            } else {
                System.out.println("query failed");
            }
            
            // Retrieve the value of the "Output" variable from each response
            ArrayList<String> oneVariableAnswer = getOneVariableAnswer(executeGoal, "Output");
            return oneVariableAnswer;
            
        // Exception handlers
        } catch (PrologException ex) {
            Logger.getLogger(QueryWineDatabase.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } catch (Exception ex) {
            Logger.getLogger(QueryWineDatabase.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    /** Search for a variable in each response and get the list of variable values
     * 
     * @param answer the set of query responses (PROLOG assignments)
     * @param variable a variable to search for
     * @return the list of PROLOG terms corresponding to the given variable
     */
    ArrayList<String> getOneVariableAnswer(SetOfPrologAssignments answer, String variable){
        
        // List of terms to be returned
        ArrayList<String> names = new ArrayList<String>();
        
        // Iterate through assignments
        Iterator<PrologAssignments> iter = answer.iterator();
        while (iter.hasNext())
        {
            PrologAssignments assign = iter.next();
            // get the set of terms for the current assigmnent
            Set<Term> setOfNames = assign.getSetOfNames(); 
            
            // Iterate through terms
            Term name = null;
            Iterator<Term> termsIter = setOfNames.iterator();
            // Loop until the match to the variable is found
            while (termsIter.hasNext() && name == null)
            {
                Term currentTerm = termsIter.next();
                if (currentTerm.toString().equals(variable)) 
                    name = currentTerm;
            }

            // Get the value of the variable and add it to the list
            Term findAssignment = assign.findAssignment(name);
            String assignS = findAssignment.toString();
            names.add(assignS);
        }
        return names;
    }
   }
