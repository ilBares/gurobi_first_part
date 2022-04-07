package it.unibs.operations_research.gurobi.couple_10;
import gurobi.*;

import java.util.Arrays;

public class EntryPoint {
    // number of television station
    private static final int M = 10;

    // number of time slots for each television station
    private static final int K = 8;

    // minimum daily number of spectators (coverage)
    private static final int S = 86236;

    // Ω% (Omega) - percentage of the minimum budget to invest in each time slot
    private static final int O = 2;

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

    // Cost euro/minute of each time slot
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

    // Coverage of spectators (spectators/minute) guaranteed by spending C_ij euro/minute
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


    // total budget
    private static final int B_TOT = Arrays.stream(B_i).sum();

    // 2% of the total budget
    private static final double B_PCT = (B_TOT/100.)*2;

    public static void main(String[] args) throws GRBException {
        // GRBEve stands for 'Gurobi Environment'
        // we will add parameters to the environment to solve problems
        // to set parameters: 'env.set(<parameter>, <value>)'
        // main parameters are:
        // GRB.IntParam.Threads         # number of Threads used by Gurobi
        // GRB.IntParam.Presolve        # operations before the executions of our model - speedup the execution
        // GRB.DoubleParam.TimeLimit    # time limit dedicated to Gurobi to solve our problem
        GRBEnv env = new GRBEnv("gurobi_first_part.log");

        // it sets necessary parameters
        setParameters(env);

        // a model represents a single optimization problem
        // it contains set of variables, set of constraints, one objective function and others attributes
        GRBModel model = new GRBModel(env);

        // adding x_ij variables
        GRBVar[][] x_ij = addVariables(model);

        // adding slack surplus variables
        GRBVar[] s = addSlackSurplusVariables(model);

        // adding budget constraints
        addBudgetConstraints(model, x_ij, s);

        // adding minimum spectators number
        addSpectatorsConstraint(model, x_ij, s);

        setObjectiveFunction(model, x_ij);

        model.update();
        // to optimize our model
        model.optimize();

        //////////////////
        int status = model.get(GRB.IntAttr.Status);

        // results can be analyzed with '<constraint>.get(...)' or 'model.get(...)'
        // result of optimization is contained in 'Status' attribute
        // 'GRB.StringAttr.VarName' is the name of the variable
        // 'GRB.DoubleAttr.X' contains the value of the variable in current solution
        // 'GRB.DoubleAttr.ObjVal' contains the value of the objective function in current solution
        model.get(GRB.DoubleAttr.ObjVal);

        System.out.println("\n\nStatus: " + status);

        printHeader();

        printFirstQuestion(model, 0 , 0, 0);

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
                x_ij[i][j] = model.addVar(0.0, T_ij[i][j], 0.0, GRB.CONTINUOUS, "xij_" + (i + 1) + "_" + (j + 1));
            }
        }

        return x_ij;
    }

    private static GRBVar[] addSlackSurplusVariables(GRBModel model) throws GRBException {
        // M == Bi.length() : constraints related to the maximum budget for each television station
        // K : constraints related to minimum budget for each time slot
        // 1 : constraint related to minimum total number of spectators (daily)
        GRBVar[] s = new GRBVar[M + K + 1];

        for (int i = 0; i < (M + K + 1); i++)
            s[i] = model.addVar(0.0, GRB.INFINITY, 0.0, GRB.CONTINUOUS, "s_" + i);

        return s;
    }

    private static void setObjectiveFunction(GRBModel model, GRBVar[][] x_ij) throws GRBException {
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

    private static void addBudgetConstraints(GRBModel model, GRBVar[][] x_ij, GRBVar[] s) throws GRBException {
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
            model.addConstr(expr, GRB.EQUAL, B_PCT, "c_min_budget_" + (j+1));
        }
    }

    private static void addSpectatorsConstraint(GRBModel model, GRBVar[][] x_ij, GRBVar[] s) throws GRBException {
        GRBLinExpr expr = new GRBLinExpr();

        for (int i = 0; i < M; i++) {
            for (int j = 0; j < K; j++) {
                expr.addTerm(P_ij[i][j], x_ij[i][j]);
            }
        }
        expr.addTerm(-1.0, s[M+K]);
        model.addConstr(expr, GRB.EQUAL, S, "c_spectators");
    }


    private static void printHeader() {
        System.out.println("GRUPPO 10\n" +
                "Componenti: Baresi, El Koudri");
    }

    private static void printFirstQuestion(GRBModel model, int fullCoverage, double purchasedTime, double unusedBudget) throws GRBException {
        String solution = "QUESITO I:\n" +
                "funzione obiettivo = " + model.get(GRB.DoubleAttr.ObjVal) + "\n" +
                "copertura raggiunta totale (spettatori) = " + fullCoverage + "\n" +
                "tempo acquistato (minuti) = " + purchasedTime + "\n" +
                "budget inutilizzato = " + unusedBudget + "\n" +
                "soluzione di base ottima:\n";

        for (GRBVar v : model.getVars()) {
            solution += "\n" + v.get(GRB.StringAttr.VarName) + " = " + v.get(GRB.DoubleAttr.X);
        }

        System.out.println(solution);
    }


}
