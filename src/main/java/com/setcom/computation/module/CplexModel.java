package com.setcom.computation.module;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;

import java.util.ArrayList;
import java.util.List;


public class CplexModel {
    private IloCplex cplex;
    private IloNumVar[] x;
    private IloLinearNumExpr obj;
    private List<IloRange> constraints = null;
    private boolean isSolved = false;


    /**
     * Constructor of Cplex model class, which after receiving LP problem
     * automatically compute solution.
     *
     * @param a Constraint coefficient matrix
     * @param b Capacity constraint vector
     * @param c Cost vector
     * @param m Number of constraints
     * @param n Number of variables
     * @param isMaximize Maximization or minimization problem
     */
    public CplexModel(double[][] a, double[] b, double[] c, int n, int m, boolean isMaximize) {
        try {
            cplex = new IloCplex();
            x = initDecisionVarArray(n);
            obj = defineObjectiveFunction(c);
            defineConstraints(a, b, m, n, isMaximize);

            // Suppress the auxiliary output printout
            cplex.setParam(IloCplex.IntParam.SimDisplay, 0);

            // Solve the model and print the output
            isSolved = cplex.solve();

        } catch (IloException e) {
            e.printStackTrace();
        }
    }

    public double getObjValue() throws IloException {
        if (isSolved) {
            return cplex.getObjValue();
        } else {
            return -1.0d;
        }
    }

    public double[] getDecisionVariable() throws IloException {
        if (isSolved) {
            double[] result = new double[x.length];
            for (int i = 0; i < x.length; i++) {
                result[i] = cplex.getValue(x[i]);
            }
            return result;
        } else {
            return new double[0];
        }
    }

    public double[] getArrayOfReducedCosts() throws IloException {
        if (constraints != null) {
            double[] result = new double[x.length];
            for (int i = 0; i < x.length; i++) {
                result[i] = cplex.getReducedCost(x[i]);
            }
            return result;
        } else {
            // co w przypadku maksymalizacji? Tablica zysków????
            return null;
        }
    }

    public void checkForBindingConstraints() throws IloException {
        // Check for binding/non-binding constraints
        for(int i = 0; i < constraints.size(); i++) { // for each constraint
            double slack = cplex.getSlack(constraints.get(i));
            double dual = cplex.getDual(constraints.get(i));
            if(slack != 0) {
                System.out.println("Constraint " + (i+1) + " is non-binding.");
            } else {
                System.out.println("Constraint " + (i+1) + " is binding.");
            }
            System.out.println("Shadow price = " + dual);
            System.out.println();
        }
    }




    /**
     * Initialize an array of decision variables.
     * Each variable has range from 0 to +INF
     *
     * @param arraySize - number of decision variables
     * @return - an array of decision variables
     * @throws IloException - passed from IloCplex library
     */
    private IloNumVar[] initDecisionVarArray(int arraySize) throws IloException {
        IloNumVar[] x = new IloNumVar[arraySize];
        for (int i = 0; i < arraySize; i++) {
            x[i] = cplex.intVar(0, Integer.MAX_VALUE);
        }
        return x;
    }

    /**
     *  Define objective function and add expressions to it.
     *
     * @param c - cost vector
     * @return - objective function represented as IloLinearNumExpr object
     * @throws IloException - passed from IloCplex library
     */
    private IloLinearNumExpr defineObjectiveFunction(double[] c) throws IloException {
        IloLinearNumExpr obj = cplex.linearNumExpr();
        for (int i = 0; i < x.length; i++) {
            obj.addTerm(c[i], x[i]);
        }
        return obj;
    }

    private void defineConstraints(double[][] A, double[] b, int m, int n, boolean isMaximization) throws IloException {
        if (isMaximization) {
            cplex.addMaximize(obj);
            for (int i = 0; i < m; i++) {
                cplex.addLe(createConstraint(A, n, i), b[i]); // define RHS constraints
            }
        } else {
            cplex.addMinimize(obj);
            constraints = new ArrayList<>();
            for (int i = 0; i < m; i++) {
                constraints.add(cplex.addGe(createConstraint(A, n, i), b[i]));
            }
        }
    }

    private IloLinearNumExpr createConstraint(double[][] A, int n, int i) throws IloException {
        IloLinearNumExpr constraint = cplex.linearNumExpr();
        for (int j = 0; j < n; j++) {
            constraint.addTerm(A[i][j], x[j]);
        }
        return constraint;
    }

}
