JadeLab
=======

Multi agent system lab.

Three questions about wines are asked from an agent and another agent answers.

The program collects different properties from wines with SPARQL and OWL Wine ontology.
The Wine information is later made into PROLOG predicates for the agents to reason about
Example prolog predicate. wine(corbansdrywhiteriesling,corbans,newzealandregion,full,strong,dry,no_color).

The response agent answers in XML format back to the agent who asked the question.
The query agent asks the response agent in this form "wine(Output,_,_,_,_,dry,_)" Which means give me the name of a wine that is dry.
An example response from this would then be corbansdrywhiteriesling (looking at the prolog predicate above)
Where output is the answer you want, and the other are inputs for wine properties. 
The different parameters 


Science!
