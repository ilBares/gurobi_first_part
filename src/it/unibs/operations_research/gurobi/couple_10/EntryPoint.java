package it.unibs.operations_research.gurobi.couple_10;
import gurobi.*;

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

    public static void main(String[] args) throws GRBException {
        // GRBEve stands for 'Gurobi Environment'
        // we will add parameters to the environment to solve problems
        // to set parameters: 'env.set(<parameter>, <value>)'
        // main parameters are:
        // GRB.IntParam.Threads         # number of Threads used by Gurobi
        // GRB.IntParam.Presolve        # operations before the executions of our model - speedup the execution
        // GRB.DoubleParam.TimeLimit    # time limit dedicated to Gurobi to solve our problem
        GRBEnv env = new GRBEnv("gurobi_first_part.log");

        // we can set solve method used by Gurobi
        // '0' stands for "primal simplex"
        env.set(GRB.IntParam.Method, 0);

        // we choose to disable gurobi presolve option
        // to avoid unexpected changes
        env.set(GRB.IntParam.Presolve, 0);



    }
}
