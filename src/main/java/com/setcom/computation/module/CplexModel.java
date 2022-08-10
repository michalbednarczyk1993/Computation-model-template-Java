package com.setcom.computation.module;

import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;


public class CplexModel {
    IloCplex cplex;
    private DirectedGraph<Object,String> instance;

    CplexModel() throws IloException {
        cplex = new IloCplex();
    }

    private void addVariables() throws IloException {
        for (Object i : instance.getNodes()) {
            IloNumVar var = cplex.boolVar();
            varMap.put(i, var);
        }
    }
}
