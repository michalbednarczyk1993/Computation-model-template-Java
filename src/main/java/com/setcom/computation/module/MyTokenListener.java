package com.setcom.computation.module;

import com.setcom.computation.balticlsc.*;
import com.setcom.computation.datamodel.Status;
import ilog.concert.*;
import ilog.cplex.*; // CPLEX
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.mongodb.core.aggregation.ArithmeticOperators;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class MyTokenListener extends TokenListener {

        public MyTokenListener(IJobRegistry registry, IDataHandler data) {
                super(registry, data);
        }

        @Override
        public void dataReceived(String pinName) {
                registry.setStatus(Status.WORKING);
                try {
                        String folderPath = data.obtainDataItem("Folder Name"); // Item / items
                        // In case the data is simple and is wholly contained in the tokens,
                        // you should use operations from the IJobRegistry interface:
                        //  GetPinValue, GetPinValues, GetPinValuesNDim
                        //registry.getPinValue();
                        // chyba powinno operować na GetPinValuesNDim

                        if (Files.isDirectory(Paths.get(folderPath))) {
                                List<Path> filePathList = Files.list(Paths.get(folderPath)).collect(Collectors.toList());
                                int i = 0;
                                for (Path path : filePathList) {
                                        File f = new File(String.valueOf(path));

                                        // mogę przetwarzać jakoś te dane

                                        // Po Przetworzeniu danych
                                        // data.sendDataItem(path);
                                        // In case of data wholly contained in the tokens
                                        // (or when you handle sending data manually),
                                        // you should use the SendToken operation.

                                        registry.setProgress(++i/filePathList.size());
                                }
                        } else {
                                File file = new File(String.valueOf(folderPath));

                                // mogę przetwarzać jakoś te dane

                        }
                } catch (Exception e) {
                        log.error(e.toString());
                        registry.setStatus(Status.FAILED);
                        return;
                }
                data.finishProcessing();
        }

        public void optionalDataReceived(String pinName)
        {
            // Place your code here:

        }

        public void dataReady()
        {
            // Place your code here:

        }

        public void dataComplete()
        {
            // Place your code here:

        }

        /**
         * Defines the solves the LP.
         *
         * @param n Number of variables
         * @param m Number of constraints
         * @param c Cost vector
         * @param A Constraint coefficient matrix
         * @param b Capacity constraint vector
         */
        private void compute(int n, int m, double[][] A, double[] b, double[] c, boolean isMaximize) {
                try {
                        IloCplex cplex = new IloCplex(); // an empty model

                        // An array of decision variables
                        // each variable has range from 0 to +INF
                        IloNumVar[] x = new IloNumVar[n];
                        for (int i = 0; i < n; i++) {
                                x[i] = cplex.intVar(0, Integer.MAX_VALUE);
                        }

                        // Define objective function
                        // Add expressions to objective function (obj)
                        IloLinearNumExpr obj = cplex.linearNumExpr();
                        for (int i = 0; i < n; i++) {
                                obj.addTerm(c[i], x[i]);
                        }

                        // Choose between maximization and minimization
                        if (isMaximize) cplex.addMaximize(obj);
                        else cplex.addMinimize(obj); // różni się dla minimize (constraints)

                        // Define constraints
                        for (int i = 0; i < m; i++) {
                                IloLinearNumExpr constraint = cplex.linearNumExpr();
                                for (int j = 0; j < n; j++) {
                                        constraint.addTerm(A[i][j], x[j]);
                                }
                                cplex.addLe(constraint, b[i]); // define RHS constraints
                        }

                        // Suppress the auxiliary output printout
                        cplex.setParam(IloCplex.IntParam.SimDisplay, 0);

                        // Solve the model and print the output

                        if (cplex.solve()) {
                                double objValue = cplex.getObjValue();
                                System.out.println("obj_val = " + objValue);

                                for(int i = 0; i < n; i++) {
                                        System.out.println("x[" + (i+1) + "] = " + cplex.getValue(x[i]));
                                }
                        } else {
                                System.out.println("Model not solved");
                        }




                } catch (IloException e) {
                        e.printStackTrace();
                }



        }
}
