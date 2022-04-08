package it.unibs.operations_research.gurobi.couple_10;

import gurobi.*;
import java.util.Arrays;

/**
 * Gurobi Project part I.
 *
 * @author Baresi Marco
 * @author El Koudri Chaimaa
 */
public class Gurobi {
    // number of television station
    private static final int M = 10;

    // number of time slots for each television station
    private static final int K = 8;

    // M == Bi.length() : number of constraints related to the maximum budget for each television station
    // K : number of constraints related to minimum budget for each time slot
    // 1 : number of constraint related to minimum total number of spectators (daily)
    private static final int SLACK_NUM = M + K + 1;

    // minimum daily number of spectators (coverage)
    private static final int S = 86236;

    // Ω% (Omega) - percentage of the minimum budget to invest in each time slot (2%)
    private static final double O = 0.02;

    // β_i - maximum budget for the i-th television station
    private static final int [] B_i = {
            3356,
            2632,
            2867,
            3215,
            3103,
            3449,
            2825,
            3398,
            3158,
            2657
    };

    // τ_ij - maximum amount of minutes that can be purchased in each time slot
    // "i": index to the television station
    // "j": index to the time slot
    private static final int [][] T_ij = {
            {1, 2, 2, 1, 1, 2, 2, 1},
            {2, 2, 1, 2, 2, 2, 2, 3},
            {1, 1, 2, 1, 1, 2, 2, 3},
            {3, 3, 1, 2, 2, 1, 2, 2},
            {2, 1, 2, 3, 2, 2, 2, 1},
            {2, 2, 2, 3, 2, 3, 1, 1},
            {2, 3, 2, 3, 2, 3, 3, 2},
            {2, 2, 1, 1, 3, 2, 1, 1},
            {3, 2, 2, 2, 3, 1, 3, 2},
            {2, 2, 2, 2, 3, 3, 1, 2}
    };

    // cost euro/minute of each time slot
    // "i": index to the television station
    // "j": index to the time slot
    private static final int [][] C_ij = {
            {914, 972, 1352, 1299, 1258, 1237, 1276, 1286},
            {1030, 969, 1073, 1234, 1289, 1107, 1357, 1276},
            {1270, 1191, 1393, 1112, 1297, 1296, 1244, 1228},
            {1390, 1121, 1009, 1039, 1107, 993, 1144, 1073},
            {1237, 1345, 1191, 1235, 954, 1314, 976, 953},
            {1065, 1012, 1349, 1145, 1087, 938, 1343, 1356},
            {1235, 970, 998, 900, 1064, 1178, 970, 1056},
            {1159, 1077, 1330, 1261, 1294, 1382, 1190, 1002},
            {996, 1137, 1151, 931, 1067, 986, 1014, 1104},
            {1101, 1354, 1381, 1026, 1374, 986, 1067, 1149}
    };

    // coverage of spectators (spectators/minute) guaranteed by spending C_ij euro/minute
    // "i": index to the television station
    // "j": index to the time slot
    private static final int [][] P_ij = {
            {1387, 3382, 3496, 1574, 1292, 1989, 2251, 1314},
            {919, 3333, 595, 956, 1299, 2485, 3241, 1642},
            {1546, 2036, 1493, 2429, 2325, 1840, 1124, 3088},
            {2781, 784, 1133, 1203, 1990, 2333, 1046, 2569},
            {2308, 3480, 628, 2628, 2606, 384, 3413, 2764},
            {2348, 392, 3480, 1005, 553, 2536, 2367, 1461},
            {3014, 931, 3194, 1926, 2482, 2680, 947, 359},
            {2712, 3405, 680, 2389, 1517, 2085, 953, 3021},
            {2421, 886, 781, 3246, 3142, 601, 813, 735},
            {911, 2714, 2837, 3135, 3007, 409, 898, 1598}
    };


    // total budget = sum of maximum budgets of the i-th television station
    private static final int B_TOT = Arrays.stream(B_i).sum();

    // 2% of the total budget = B_TOT * 0.02
    private static final double B_PCT = B_TOT*O;

    // entry point for our Gurobi Project
    public static void main(String[] args) throws GRBException {
        // GRBEve stands for 'Gurobi Environment'
        // we will add parameters to the environment to solve problems
        // to set parameters: 'env.set(<parameter>, <value>)'
        // main parameters are:
        // GRB.IntParam.Threads         # number of Threads used by Gurobi
        // GRB.IntParam.Presolve        # operations before the executions of our model - it speedups the execution
        // GRB.DoubleParam.TimeLimit    # time limit dedicated to Gurobi to solve our problem
        GRBEnv env = new GRBEnv("gurobi_first_part.log");

        // setting necessary parameters
        setParameters(env);

        // a model represents a single optimization problem
        // it contains set of variables, set of constraints, one objective function and others attributes
        GRBModel model = generateModel(env, false);

        // function to solve all required problems
        solve(env, model);

        // Release the resources associated with a GRBModel object
        model.dispose();

        // Release the resources associated with a GRBEnv object
        env.dispose();
    }

    private static void setParameters(GRBEnv env) throws GRBException {
        // we can set solve method used by Gurobi
        // '0' stands for "primal simplex"
        env.set(GRB.IntParam.Method, 0);

        // we choose to disable gurobi presolve option
        // it is necessary to avoid unexpected changes
        env.set(GRB.IntParam.Presolve, 0);
    }

    private static GRBModel generateModel(GRBEnv env, boolean isAuxiliary) throws GRBException {
        // a model represents a single optimization problem
        // it contains set of variables, set of constraints, one objective function and others attributes
        GRBModel model = new GRBModel(env);

        // adding x_ij variables
        GRBVar[][] x_ij = addVariables(model);

        // adding slack surplus variables
        // "s" stands for "slack"
        GRBVar[] s = addAdditionalVariables(model, "s");

        GRBVar[] y = null;
        if (isAuxiliary)
            // required to set auxiliary variables with the aim of finding a feasible solution that is not optimal
            y = addAdditionalVariables(model, "a");

        // adding budget constraints
        addBudgetConstraints(model, x_ij, s, y, isAuxiliary);

        // adding minimum spectators number
        addSpectatorsConstraint(model, x_ij, s, y, isAuxiliary);

        if (isAuxiliary)
            setAuxiliaryObjectiveFunction(model, y);
        else
            setObjectiveFunction(model, x_ij);

        model.update();
        // to optimize our model
        model.optimize();

        return model;
    }

    private static GRBVar[][] addVariables(GRBModel model) throws GRBException {
        // "i": index to the television station (0 ... M-1)
        // "j": index to the time slot (0 ... K-1)
        GRBVar[][] x_ij = new GRBVar[M][K];

        for (int i=0; i<M; i++) {
            for (int j = 0; j < K; j++) {
                // 'addVar' is required to add variables
                // first parameter represents the 'lower bound | lb' - in that case 0.0
                // second parameter represents the 'upper bound | ub' - in that case τ_ij
                // third parameter represents the coefficient of the variable in our objective function
                // the third parameter set to 0 is temporary, we will change it building objective function
                // fourth parameter represents the type of the variable
                // fifth parameter represents the name of the variable
                x_ij[i][j] = model.addVar(0.0, T_ij[i][j], 0.0, GRB.CONTINUOUS, "x_" + (i + 1) + "_" + (j + 1));
            }
        }

        return x_ij;
    }

    // used to add slack variables and auxiliary variables
    private static GRBVar[] addAdditionalVariables(GRBModel model, String baseVarName) throws GRBException {
        GRBVar[] vars = new GRBVar[SLACK_NUM];

        for (int i = 0; i < (M + K + 1); i++)
            vars[i] = model.addVar(0.0, GRB.INFINITY, 0.0, GRB.CONTINUOUS, baseVarName + "_" + i);

        return vars;
    }

    // y array contains variables only if "isAuxiliary" == true, we use it to solve an auxiliary problem
    private static void addBudgetConstraints(GRBModel model, GRBVar[][] x_ij, GRBVar[] s, GRBVar[] y, boolean isAuxiliary) throws GRBException {
        GRBLinExpr expr;

        // maximum budget for each television station
        for (int i = 0; i < M; i++) {
            expr = new GRBLinExpr();
            for (int j = 0; j < K; j++) {
                expr.addTerm(C_ij[i][j], x_ij[i][j]);
            }
            // adding slack
            expr.addTerm(1, s[i]);
            model.addConstr(expr, GRB.EQUAL, B_i[i], "c_max_budget_" + (i+1));
        }

        // minimum budget for each time slot
        for (int j = 0; j < K; j++) {
            expr = new GRBLinExpr();
            for (int i = 0; i < M; i++) {
                expr.addTerm(C_ij[i][j], x_ij[i][j]);
            }
            // adding slack
            expr.addTerm(-1.0, s[M+j]);
            if (isAuxiliary)
                expr.addTerm(1.0, y[M+j]);
            model.addConstr(expr, GRB.EQUAL, B_PCT, "c_min_budget_" + (j+1));
        }
    }

    // y array contains variables only if "isAuxiliary" == true, we use it to solve an auxiliary problem
    private static void addSpectatorsConstraint(GRBModel model, GRBVar[][] x_ij, GRBVar[] s, GRBVar[] y, boolean isAuxiliary) throws GRBException {
        GRBLinExpr expr = new GRBLinExpr();

        for (int i = 0; i < M; i++) {
            for (int j = 0; j < K; j++) {
                expr.addTerm(P_ij[i][j], x_ij[i][j]);
            }
        }
        expr.addTerm(-1.0, s[M+K]);
        if (isAuxiliary)
            expr.addTerm(1.0, y[M+K]);
        model.addConstr(expr, GRB.EQUAL, S, "c_spectators");
    }

    private static void setObjectiveFunction(GRBModel model, GRBVar[][] x_ij) throws GRBException {
        // 'GRBLinExpr' is used to build our function
        // 'model.setObjective(...)' sets our expression as objective function
        GRBLinExpr sum = new GRBLinExpr();
        GRBLinExpr reverse_sum = new GRBLinExpr();

        // 1.0 indicates the coefficient of the aux_var in our objective function
        GRBVar aux_var = model.addVar(0.0, GRB.INFINITY, 0.0, GRB.CONTINUOUS, "aux");

        for (int i = 0; i < M; i++) {
            for (int j = 0; j < K; j++) {
                int sign = (j < (K/2) ? 1 : -1);
                sum.addTerm(sign * P_ij[i][j], x_ij[i][j]);
                reverse_sum.addTerm((-1 * sign) * P_ij[i][j], x_ij[i][j]);
            }
        }

        model.addConstr(aux_var, GRB.GREATER_EQUAL, sum, "c_aux1");
        model.addConstr(aux_var, GRB.GREATER_EQUAL, reverse_sum, "c_aux2");

        GRBLinExpr objFunc = new GRBLinExpr();
        objFunc.addTerm(1.0, aux_var);

        model.setObjective(objFunc, GRB.MINIMIZE);
    }

    private static void setAuxiliaryObjectiveFunction(GRBModel model, GRBVar[] y) throws GRBException {
        // 'GRBLinExpr' is used to build our function
        // 'model.setObjective(...)' sets our expression as objective function
        // auxiliary objective function is necessary to find a feasible but not optimal solution
        GRBLinExpr obj = new GRBLinExpr();

        for (int i = 0; i < M; i++) {
            for (int j = 0; j < K; j++) {
                obj.addTerm(1.0, y[i]);
            }
        }

        model.setObjective(obj);
        model.set(GRB.IntAttr.ModelSense, GRB.MINIMIZE);
    }

    private static void solve(GRBEnv env, GRBModel model) throws GRBException {
        double objVal, fullCoverage = 0., purchasedTime = 0., usedBudget = 0.;
        String optimalSols = "", vBasisList = "", reducedCostsList = "", constrOpVertex = "", sols1 = "", sols2 = "", sols3 = "";
        boolean isMultiple = false, isDegenerate = false;

        // results can be analyzed with '<constraint>.get(...)' or 'model.get(...)'
        // result of optimization is contained in 'Status' attribute
        // 'GRB.StringAttr.VarName' is the name of the variable
        // 'GRB.DoubleAttr.X' contains the value of the variable in current solution
        // 'GRB.DoubleAttr.ObjVal' contains the value of the objective function in current solution
        objVal = model.get(GRB.DoubleAttr.ObjVal);

        GRBVar[] vars = model.getVars();
        double[] varsValues = new double[vars.length];
        for (int i = 0; i < vars.length-1; i++) {
            GRBVar var = vars[i];
            String varName = var.get(GRB.StringAttr.VarName);
            double varValue = var.get(GRB.DoubleAttr.X);
            varsValues[i] = varValue;

            // optimal solutions list required by Question I
            optimalSols += varName + " = " + roundValue(varValue) + "\n";

            // values required by Question I
            if (i < (M*K)) {
                // minutes actually purchased
                purchasedTime += varValue;
                // budget used
                usedBudget += varValue * C_ij[i / K][i % K];
                // total number of spectators - converage
                fullCoverage += varValue * P_ij[i / K][i % K];
            }

            // list that will contain only basis variables
            vBasisList += (var.get(GRB.IntAttr.VBasis) == 0 ? 0 : 1) + ", ";

            // necessary to verify if the optimal solution is degenerate
            if (var.get(GRB.IntAttr.VBasis) != 0) {
                if (varValue == 0) isDegenerate = true;
            } else if (var.get(GRB.DoubleAttr.RC) == 0) {
                isMultiple = true;
            }

            // list that will contain reduced costs
            reducedCostsList += var.get(GRB.DoubleAttr.RC) + ", ";
        }

        GRBConstr[] constrs = model.getConstrs();
        for (int i = M*K; i < (M*K + constrs.length-1); i++) {
            if (model.getVar(i).get(GRB.DoubleAttr.X) == 0)
                // constraints of the optimal vertex
                constrOpVertex += constrs[i - M*K].get(GRB.StringAttr.ConstrName) + ", ";
        }

        // we can arbitrary set iterationLimit (< of previous iterationCount) to get feasible but not optimal solution
        // in that case we set iterationLimit = IterCount/2 (of previous solution)
        GRBVar[] notOptimalVars = generateNotOptimalVars(model, model.get(GRB.DoubleAttr.IterCount)/2);
        double[] notOptimalVarsValues = new double[vars.length];
        for (int i = 0; i < notOptimalVars.length; i++) {
            varsValues[i] = notOptimalVars[i].get(GRB.DoubleAttr.X);
            sols1 += notOptimalVars[i].get(GRB.StringAttr.VarName) + " = " + roundValue(notOptimalVars[i].get(GRB.DoubleAttr.X)) + "\n";
        }

        // we create a new model of an auxiliary problem
        model = generateModel(env, true);

        for (GRBVar var : model.getVars()) {
            sols2 += var.get(GRB.StringAttr.VarName) + " = " + roundValue(var.get(GRB.DoubleAttr.X)) + "\n";
        }

        double[] z = convexCombination(varsValues, notOptimalVarsValues);
        for (int i = 0; i < z.length; i++)
            sols3 += vars[i].get(GRB.StringAttr.VarName) + " = " + roundValue(z[i]) + "\n";

        printHeader();
        printFirstQuestion(objVal, fullCoverage , purchasedTime, (B_TOT - usedBudget), optimalSols);
        printSecondQuestion(vBasisList, reducedCostsList, isMultiple, isDegenerate, constrOpVertex);
        printThirdQuestion(sols1, sols2, sols3);
    }

    private static GRBVar[] generateNotOptimalVars(GRBModel model, double iterationLimit) throws GRBException {
        model.reset();

        // by setting the IterationLimit to a value less than the number of iterations required for the optimal
        // solution, we will obtain a feasible but not optimal solution
        model.set(GRB.DoubleParam.IterationLimit, iterationLimit);

        model.update();
        // to optimize our model
        model.optimize();

        return model.getVars();
    }

    private static double[] convexCombination(double[] x, double[] y) {
        // in this case we take the midpoint, but it can contain an arbitrary value between 0 and 1
        double lambda = 0.5;
        double[] z = new double[x.length];

        for (int i = 0; i < x.length; i++) {
            double xVal = x[i];
            double yVal = y[i];
            z[i] = lambda*xVal + (1.-lambda)*yVal;
        }

        return z;
    }

    // methods for printing answers to project questions
    private static void printHeader() {
        System.out.println("\n\n\nGRUPPO 10\n" +
                "Componenti: Baresi, El Koudri");
    }

    private static void printFirstQuestion(double objVal, double fullCoverage, double purchasedTime, double unusedBudget, String optimalSols) {
        String solution = "\n\nQUESITO I:\n" +
                "funzione obiettivo = " + objVal + "\n" +
                "copertura raggiunta totale (spettatori) = " + fullCoverage + "\n" +
                "tempo acquistato (minuti) = " + roundValue(purchasedTime) + "\n" +
                "budget inutilizzato = " + unusedBudget + "\n" +
                "soluzione di base ottima:\n" + optimalSols;

        System.out.println(solution);
    }

    private static void printSecondQuestion(String vBasisList, String reducedCostsList, boolean isMultiple, boolean isDegenerate, String constrOpVertex) {
        String solution = "QUESITO II:\n" +
                "variabili in base: [" + vBasisList + "]\n" +
                "coefficienti di costo ridotto: [" + reducedCostsList + "]\n" +
                "soluzione ottima multipla: " + (isMultiple ? "Si" : "No") + "\n" +
                "soluzione ottima degenere: " + (isDegenerate ? "Si" : "No") + "\n" +
                "vincoli vertice ottimo: [" + constrOpVertex + "]";

        System.out.println(solution);
    }

    private static void printThirdQuestion(String sols1, String sols2 , String sols3) {
        String solution = "\nQUESITO III:\n" +
                "Prima soluzione ammissibile ma non ottima:\n" + sols1 +
                "\nSeconda soluzione ammissibile ma non ottima:\n" + sols2
                + "\nTerza soluzione ammissibile ma non ottima:\n" + sols3;

        System.out.println(solution);
    }

    private static double roundValue(double value) {
        return Math.round(value * 10000.0)/10000.0;
    }
}
