package dbvis.visualsummaries.strategies;
///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package dbvis.motionrugs.strategies;
//
//import com.jujutsu.tsne.TSneConfiguration;
//import com.jujutsu.tsne.barneshut.BHTSne;
//import com.jujutsu.tsne.barneshut.BarnesHutTSne;
//import com.jujutsu.utils.TSneUtils;
//import dbvis.motionrugs.data.DataPoint;
//import java.util.Arrays;
//import java.util.Comparator;
//
///**
// *
// * @author jwulms
// */
//public class TSNEStrategy implements Strategy {
//
//    private boolean stable = false;
//    private int initialDims = 2;
//    private int outputDims = 1;
//    private final int iterations = 6000;
//    private final double perplexity = 40.0;
//
//    private boolean useSeed = false;
//
//    @Override
//    public String getName() {
//        return "t-SNE";
//    }
//
//    @Override
//    public DataPoint[][] getOrderedValues(DataPoint[][] unsorted) {
//        DataPoint[][] result = new DataPoint[unsorted.length][unsorted[0].length];
//
//        //idx is an array of the indexes 
//        Integer[] idx = new Integer[unsorted[0].length];
//
//        //array to save the projected points of previous frame
//        double[][] prevValues = null;
//
//        if (useSeed) {
//            prevValues = new double[unsorted[0].length][1];
//            for (int i = 0; i < prevValues.length; i++) {
////                prevValues[seed2[i]][0] = i * 1.0/400.0;
//                prevValues[i][0] = seed3[i];
//            }
//        }
//
//        //find order per frame
//        for (int x = 0; x < unsorted.length; x++) {
//
//            System.out.println("Start of frame " + x + " //----------------------------------------------------------");
//
//            //get the input from {@code unsorted[x]}
//            double[][] input = new double[unsorted[x].length][2];
//            for (int y = 0; y < unsorted[x].length; y++) {
//                //get input
//                input[y][0] = unsorted[x][y].getX();
//                input[y][1] = unsorted[x][y].getY();
//                //set up {@idx} with indices to later sort it in the projected order
//                idx[y] = y;
//            }
//
//            //do t-SNE on the input values
//            BarnesHutTSne tsne;
//            tsne = new BHTSne();
//            TSneConfiguration config = TSneUtils.buildConfig(input, outputDims, initialDims, perplexity, //iterations, false, 0.1, true, true);
//
//            double[][] output;
//            if (stable && prevValues != null) {
//                output = tsne.tsne(config, prevValues);
//            } else {
//                output = tsne.tsne(config, null);
//            }
//            prevValues = output;
//
////            System.out.println("Previous frame ----------------------------------------------------------");
////            for (int i = 0; i < prevValues.length; i++) {
////                System.out.println("Value " + i + " is " + prevValues[i][0]);
////            }
////            System.out.println("End previous frame ------------------------------------------------------");
//
//            //sort the index array {@code idx} by comparing the projected points 
//            Arrays.sort(idx, new Comparator<Integer>() {
//                @Override
//                public int compare(final Integer o1, final Integer o2) {
//                    return Double.compare(output[o1][0], output[o2][0]);
//
//                }
//            });
//
////            prevValues = new double[unsorted[0].length][1];
////            for (int i = 0; i < prevValues.length; i++) {
////                prevValues[idx[i]][0] = i * 1.0 / 400.0;
////            }
////
//            //sort the result set after t-SNE projection
//            for (int y = 0; y < unsorted[x].length; y++) {
//                result[x][y] = unsorted[x][idx[y]];
//            }
//        }
//
//        return result;
//    }
//
//    public void setStability(boolean stable) {
//        this.stable = stable;
//    }
//
//    private final double[] seed = {2.746932454768698E-5,
//        3.054803183622464E-5,
//        5.7507956987881894E-5,
//        6.332646479343641E-5,
//        8.19258013612258E-5,
//        5.870229368524397E-5,
//        2.7980378596759137E-5,
//        4.540037496552757E-5,
//        8.575157576090002E-5,
//        1.3900099851583137E-5,
//        5.571588109681098E-5,
//        4.592910378398576E-5,
//        8.405277437916037E-5,
//        1.4571103379750206E-6,
//        7.668639236276073E-5,
//        6.633794230269561E-5,
//        4.603898942529645E-5,
//        7.290395315471625E-5,
//        2.2945590785449155E-5,
//        3.2268453381305965E-5,
//        7.550935299781174E-5,
//        2.9509980236980715E-5,
//        2.049716366568899E-6,
//        2.7390163752705345E-5,
//        3.121891852954867E-5,
//        9.403069711205903E-5,
//        7.005606132981241E-5,
//        2.9140669332246094E-5,
//        1.8024006337579657E-5,
//        9.184456585314044E-5,
//        1.1185964017895634E-5,
//        2.0716930630371467E-5,
//        1.2026937311027598E-5,
//        9.440089683359443E-5,
//        8.795146969941361E-5,
//        7.000628896526982E-5,
//        1.7661875240205082E-5,
//        6.149702945529021E-5,
//        3.788579440026608E-5,
//        3.821174037085231E-5,
//        1.7975437701035792E-5,
//        2.969958341162584E-5,
//        6.685122750044681E-5,
//        9.650275946788822E-6,
//        7.436733047233664E-5,
//        8.458489429618433E-6,
//        7.796547470082086E-5,
//        5.5027564214503704E-5,
//        7.226652301167163E-5,
//        8.886565291353263E-5,
//        1.8056956210442124E-5,
//        3.828576966233469E-5,
//        5.089035422003909E-5,
//        3.6340216274578556E-5,
//        7.437047609260217E-6,
//        9.182995325637628E-5,
//        5.6082383012925566E-6,
//        6.955355570208759E-5,
//        1.9729472375460044E-5,
//        4.3655721372618264E-5,
//        3.122951428923644E-5,
//        1.9306975864849054E-5,
//        4.582114722323195E-5,
//        2.4893749129410916E-5,
//        8.231093252454195E-5,
//        3.963854926373615E-5,
//        3.4191934234811126E-5,
//        9.247697420940543E-5,
//        9.850661698741481E-5,
//        8.221864045442743E-5,
//        7.50458051553299E-5,
//        7.523259709961922E-5,
//        8.804575654323687E-5,
//        4.438630499474601E-5,
//        9.516682872969903E-5,
//        1.2269423023629056E-5,
//        2.6832970492051137E-6,
//        7.317843796963043E-5,
//        7.924865001927212E-5,
//        2.8906362677691103E-5,
//        2.367129933067187E-5,
//        3.408524737910355E-5,
//        6.391354674525483E-5,
//        8.83798909553118E-5,
//        4.939213630150485E-7,
//        7.293256878256628E-5,
//        9.43924621110048E-5,
//        4.121357492426562E-5,
//        7.379029162574503E-5,
//        9.268846423189491E-5,
//        6.085431113613811E-5,
//        3.4536589851684406E-5,
//        3.8863186320789005E-6,
//        6.823100820116335E-6,
//        8.639176654848823E-5,
//        7.26438476237092E-5,
//        5.119697590329645E-5,
//        6.218377293918044E-5,
//        1.0311919228846712E-5,
//        9.093591581044278E-5,
//        7.121658811892757E-5,
//        3.63708424659273E-5,
//        3.328048294620375E-5,
//        3.0356951630268814E-5,
//        7.508907305163272E-5,
//        4.495329980444441E-6,
//        9.294887151262542E-5,
//        8.469676210945465E-6,
//        8.219970861442303E-5,
//        4.6372313627936416E-5,
//        9.186697822111967E-6,
//        7.536651001304223E-5,
//        1.7211153144464786E-5,
//        5.237623855294273E-5,
//        8.577495726484021E-5,
//        1.638864601127259E-5,
//        8.659135909999885E-5,
//        1.3593339768923419E-5,
//        8.365253023931696E-5,
//        5.415983730990498E-5,
//        2.621193972625464E-5,
//        8.647456152411754E-5,
//        9.090825412336188E-5,
//        9.007055115251249E-5,
//        1.9314024932828133E-5,
//        3.25749805884758E-5,
//        6.593123033451308E-5,
//        5.90134747047509E-5,
//        9.73880226673921E-5,
//        1.7100602606588644E-5,
//        4.3627831133891115E-5,
//        5.5558809921342537E-5,
//        1.5499440258059272E-5,
//        6.098198561652104E-5,
//        3.3608300814796914E-5,
//        7.729759715674381E-5,
//        1.4281191069640654E-5,
//        4.9885695033366405E-5,
//        8.85664923784174E-6,
//        2.7126579898791026E-5,
//        1.1022530953991706E-5,
//        2.1341065553304485E-5,
//        4.531087397658844E-5,
//        2.8917679615580373E-5,
//        1.4570179213079794E-5,
//        1.871138051787341E-5,
//        8.634622577324529E-5,
//        2.8029214487880594E-5,
//        1.3995166766573785E-5,
//        9.11291860424239E-5,
//        1.4304835621755275E-5,
//        6.344595592900217E-5,
//        7.264174785565156E-5,
//        7.381447063353289E-5,
//        9.804450130755178E-5,
//        5.7693813522436913E-5,
//        2.272165166736423E-5,
//        3.2833292842000285E-5,
//        9.507159812734224E-5,
//        3.701302251221164E-5,
//        4.90732943646004E-6,
//        4.12225328757161E-5,
//        3.5007525754659974E-5,
//        4.511719333987493E-5,
//        3.2787500812846214E-5,
//        9.604886795351146E-5,
//        8.525118398782917E-6,
//        6.465304284463074E-5,
//        7.054838229977656E-5,
//        9.069141368325362E-5,
//        7.399057701203459E-5,
//        3.234676472650645E-5,
//        2.0012473965742763E-5,
//        2.1003914431303883E-5,
//        7.005811092691881E-5,
//        4.282780746676798E-5,
//        2.2552399468347497E-5,
//        3.160454867845599E-5,
//        4.179669956155736E-5,
//        6.134388511865587E-5,
//        3.0671531980956646E-5,
//        6.97906746848029E-5,
//        4.13057552573924E-5,
//        4.756596840497333E-5,
//        9.754795694408951E-5,
//        2.7853178017526304E-5,
//        5.1959172110328256E-5,
//        3.866440170085399E-5,
//        8.921646378356019E-7,
//        2.104883847443194E-5,
//        7.660787158015575E-5,
//        8.86810856499818E-5,
//        6.141713858525503E-5,
//        5.260697681649101E-5,
//        3.2870630876430044E-5,
//        2.820482198901211E-5,
//        1.2210646339846976E-5,
//        4.4992772451664E-5,
//        4.667115906366085E-5,
//        5.922340446318451E-5,
//        2.9123818384996505E-5,
//        2.2845744262985015E-5,
//        6.152108252386309E-5,
//        2.2847939985855903E-5,
//        3.9331316362203565E-5,
//        9.690089325047413E-6,
//        6.985881666551492E-5,
//        4.622919225586868E-5,
//        9.269047352087874E-5,
//        5.830135342172901E-5,
//        3.058787517091815E-5,
//        7.082813481202318E-5,
//        5.3059141679344236E-5,
//        4.537281216242511E-5,
//        4.878550389080565E-5,
//        3.6506399492235746E-5,
//        1.8985648385934196E-5,
//        8.66284846991191E-5,
//        8.206030600608367E-5,
//        3.446011690659947E-5,
//        3.0138800931416057E-5,
//        1.690474341572914E-5,
//        7.410030864063966E-6,
//        8.635001359714484E-5,
//        2.2437733243349256E-5,
//        1.2222947155445841E-5,
//        9.160361629586933E-6,
//        1.7580947367033462E-5,
//        3.8404478608318E-5,
//        1.0289646896945138E-5,
//        8.005992449257822E-5,
//        1.527690630292007E-5,
//        2.1903688369419795E-5,
//        6.574338769492202E-5,
//        1.4522389075649134E-5,
//        1.0969022290774522E-5,
//        5.8433790751180716E-6,
//        4.1355922768603404E-5,
//        9.30743493893238E-6,
//        7.988158108278098E-6,
//        5.933365236230762E-5,
//        5.348969971691653E-6,
//        9.43114524896458E-5,
//        8.780119126698849E-6,
//        8.216934910610591E-5,
//        6.999081199929128E-5,
//        7.591536342380579E-6,
//        8.302077952739886E-5,
//        4.742257985753056E-5,
//        1.853398288999473E-5,
//        4.027588719362803E-5,
//        5.2406158093790134E-5,
//        7.182670010789444E-5,
//        2.904385174005755E-5,
//        8.651928784978618E-5,
//        5.6937556846368055E-5,
//        8.576437385423356E-5,
//        8.899200689619473E-5,
//        9.040222542133658E-5,
//        4.736414325546464E-5,
//        1.5255158655531443E-5,
//        7.88576028638169E-5,
//        2.2436588957112426E-5,
//        6.988729473550986E-5,
//        5.738964222672495E-5,
//        1.294137478707107E-5,
//        3.549378972390861E-5,
//        9.138992279342708E-5,
//        8.322486671757091E-5,
//        3.188312553460028E-5,
//        1.1886058497867724E-5,
//        4.323632199598548E-6,
//        8.744101120928818E-5,
//        3.22927175146725E-5,
//        8.219843098585355E-5,
//        1.262360160896111E-5,
//        8.733795469382064E-5,
//        4.656459872014555E-5,
//        9.635738864337651E-6,
//        5.093695508601115E-6,
//        8.656534840868874E-5,
//        1.4110713189285906E-5,
//        7.311541362512957E-5,
//        6.715474804994806E-5,
//        6.066410652625672E-5,
//        2.5303092327348042E-5,
//        7.234198121278362E-5,
//        1.7892504562514512E-5,
//        2.5364536239261528E-5,
//        8.482806234795362E-5,
//        2.7971578949744838E-5,
//        8.909784611962093E-5,
//        6.940592213205594E-5,
//        7.989306013286826E-5,
//        4.113186616553526E-5,
//        6.795606017108347E-6,
//        9.822884838477942E-5,
//        6.777904575723389E-5,
//        1.7521403546458037E-5,
//        6.34553287891019E-5,
//        8.870681851396608E-5,
//        5.939638658009715E-5,
//        7.809330387631675E-5,
//        8.993471176883552E-5,
//        1.303731061005986E-6,
//        4.927361227255689E-5,
//        1.852425982703946E-5,
//        6.42899062650078E-5,
//        1.877365120095509E-7,
//        6.605703771511168E-5,
//        9.750109089715565E-5,
//        6.725653472496891E-5,
//        1.1442758575999413E-5,
//        3.284843958550954E-5,
//        5.491550884406583E-5,
//        1.3860948483157999E-5,
//        3.109285796263E-5,
//        2.301055158628558E-5,
//        4.550993738219456E-5,
//        5.363287256995164E-5,
//        2.6955040166010616E-5,
//        1.284321399441194E-5,
//        1.5850214767225958E-5,
//        9.946690453258533E-6,
//        3.292042303156665E-5,
//        2.1161367739119477E-6,
//        9.191365972903334E-5,
//        8.359837574181671E-5,
//        8.129855118249329E-5,
//        9.578461661323011E-5,
//        7.239525494991792E-5,
//        5.4771451866728843E-5,
//        3.786517831013359E-5,
//        8.462851685232914E-5,
//        9.637663995247305E-5,
//        1.1335203021374984E-5,
//        5.7387620899345527E-5,
//        6.961507953014224E-5,
//        6.502658732026805E-5,
//        8.054922595352682E-5,
//        3.122435245764184E-5,
//        3.8320290663122917E-5,
//        3.9724131960910706E-5,
//        5.051448184265395E-6,
//        8.754408675136243E-5,
//        8.088873995873284E-5,
//        2.5501213169796446E-5,
//        7.388641755530082E-5,
//        5.0902241158627595E-5,
//        6.595423709492975E-5,
//        5.017792817484247E-5,
//        9.357885299883556E-5,
//        9.085537615749352E-6,
//        5.6473983321112144E-5,
//        1.1996672836752654E-5,
//        2.1680129283326556E-5,
//        1.6707471013908226E-5,
//        6.367289598980624E-5,
//        5.821284620254626E-5,
//        6.595261718102401E-5,
//        4.1097578628677555E-5,
//        8.492101303723359E-5,
//        5.232782222317295E-5,
//        4.0178827865038106E-5,
//        7.10048622516493E-5,
//        3.97896279197191E-5,
//        8.019208905685291E-5,
//        5.637971929328162E-5,
//        8.602128087098387E-5,
//        6.324637289591117E-5,
//        5.717066684845328E-5,
//        1.2822979100589904E-5,
//        4.125004525217413E-5,
//        3.0447580877616256E-5,
//        5.550131467345031E-5,
//        5.100720228195991E-5,
//        7.515371310364985E-5,
//        7.132551426461359E-5,
//        6.668223167361386E-5,
//        3.719463957604874E-6,
//        9.871738248006701E-5,
//        9.248794626116816E-5,
//        6.815694584728456E-5,
//        8.174774509597293E-5,
//        3.475781441249272E-5,
//        7.97024224553272E-5,
//        7.950060584903043E-5,
//        4.647651708541184E-5,
//        7.862263696527887E-5,
//        8.591546545492221E-5,
//        1.8567564964433916E-5,
//        5.27721071557498E-5,
//        8.855054866265678E-5,
//        5.598989326497849E-5,
//        4.2815308358049E-5,
//        1.1529466601759054E-5,
//        2.4269630403568354E-5,
//        4.056813753286479E-5,
//        6.942047568493297E-5,
//        9.14329412011984E-5};
//
//    private final double[] seed3 = {-4.376889953401795,
//        6.033812267023151,
//        -5.122643256844984,
//        2.134205767796914,
//        -5.57624364811389,
//        -2.5378710503873574,
//        -2.655473606100234,
//        6.694152283957381,
//        0.3733965250951498,
//        5.1117454971610705,
//        1.8156598894803904,
//        -6.641380207063592,
//        0.386757651741123,
//        4.921914089170118,
//        1.6053791143197915,
//        6.471700190490177,
//        -5.928608964464976,
//        -0.7440313832505473,
//        -5.149152037463728,
//        -0.7544575790072992,
//        3.6272571308481405,
//        3.4562000689068,
//        7.472998245968588,
//        3.8010203184081184,
//        6.13556871203173,
//        -9.277206291425049,
//        -3.907976143353683,
//        1.2476457127239724,
//        -1.6930644812063,
//        2.02841167033716,
//        7.970810238961983,
//        -6.799613261865311,
//        -0.5272706714678694,
//        1.3082655974840103,
//        -2.6926186712843045,
//        4.732492234726648,
//        2.482415994477881,
//        -4.670369886437225,
//        -2.57379762475484,
//        -1.6336272766390445,
//        -2.449721834715469,
//        7.049767219329793,
//        -1.5455008331211941,
//        7.800526363795131,
//        5.915073608916688,
//        -3.8499055349161293,
//        5.104350080921149,
//        -2.128600729994085,
//        -1.071628026823721,
//        6.255769829646128,
//        2.746724662428723,
//        7.684717408106795,
//        -6.797786267351516,
//        3.7772832700741135,
//        0.45473387935554216,
//        7.621183207272573,
//        -3.258574376825734,
//        -7.588824827737288,
//        0.8605623107618741,
//        6.296821328595179,
//        -3.927743855909787,
//        1.4441746002274254,
//        3.3032908546264887,
//        -8.157136813572844,
//        -9.996784899831635,
//        4.9229334581551445,
//        7.6070292512347795,
//        5.330546983868704,
//        -2.6489661974315593,
//        1.1878262992986897,
//        3.833281201121205,
//        -2.0821318432622204,
//        1.6934800512700217,
//        -5.104893765610659,
//        6.431001961833041,
//        -1.8879360821384708,
//        -6.848125687206942,
//        -0.8390324357307782,
//        0.7555715353500996,
//        1.888218630155294,
//        -5.051571486383206,
//        3.910845620945287,
//        4.889413545156293,
//        5.5459507440272775,
//        -5.121396711861886,
//        7.0936782167824575,
//        4.3361913253819955,
//        -6.34722161078948,
//        4.838844331412568,
//        7.777038759484647,
//        -6.6334348934142,
//        0.9503811078261153,
//        5.941127376776577,
//        3.76202356925345,
//        -3.651140740807268,
//        4.639073804359039,
//        -6.178249626719359,
//        3.2734122124113663,
//        0.44027646308569146,
//        4.720502010278745,
//        2.6504729609307995,
//        6.0858636846913905,
//        -4.198279588396276,
//        7.475420668914035,
//        -1.2464346069637697,
//        -8.983507875576619,
//        -2.6333807393283037,
//        -1.4906236010657372,
//        6.604056682549336,
//        -8.580817061889471,
//        -3.7152611290911928,
//        -3.0886904551704517,
//        7.334186256063787,
//        4.656476688365351,
//        -5.738219648642063,
//        -0.7489081496899967,
//        6.282341868769532,
//        3.941419044083323,
//        4.802003383840201,
//        -3.0289802637519196,
//        0.6966535773973986,
//        -1.8179412737737144,
//        2.292799738077709,
//        2.0172009812908906,
//        0.6179482700266554,
//        -7.09004206635111,
//        2.884266227536974,
//        -4.946245072041066,
//        -2.780199889146308,
//        4.46607785148169,
//        5.401963191548326,
//        2.5320661870493057,
//        -6.229835085481628,
//        -4.893182531934174,
//        -5.834051043099702,
//        2.125437671472397,
//        -1.1163924716734372,
//        -6.113420872417112,
//        -2.4888834878722057,
//        -3.59574374509283,
//        -2.6385459967247047,
//        5.894643983158508,
//        -6.221069977261952,
//        -6.183012557772766,
//        -5.518209904275102,
//        5.993918764642659,
//        4.736886968223812,
//        6.254874038780389,
//        7.891489044604805,
//        0.8421831526626365,
//        -2.788046986077693,
//        3.4670271921245677,
//        -1.8146282463153567,
//        4.405158891206153,
//        -2.922444926842981,
//        6.7191789215278925,
//        0.8095134224893273,
//        -6.806058076956288,
//        0.8025793672629072,
//        -5.231842672633147,
//        -3.026127850030257,
//        -4.784320427721498,
//        4.498088698261843,
//        -3.5202571927090336,
//        -6.734267845034437,
//        0.9897818440653815,
//        3.6093678162500438,
//        7.069732494438551,
//        0.6383927353102394,
//        -4.888593151811324,
//        -7.969345451775821,
//        -5.875101988701947,
//        5.989853158098869,
//        5.670261279238813,
//        2.019098242629644,
//        -3.2491623253071733,
//        -4.891232749088337,
//        5.9413247081101535,
//        -3.992151419368954,
//        -4.52741232069132,
//        1.671381511258649,
//        5.290438739939068,
//        -6.037483135755314,
//        -6.27381979891137,
//        4.61862230177763,
//        5.135806949136426,
//        7.318895142977586,
//        -0.5234222019736073,
//        6.447829363318003,
//        0.7224199830150543,
//        7.189337625789355,
//        2.6933208781988207,
//        -6.243228660012537,
//        -5.498164894495409,
//        1.7430088875728282,
//        4.86354374315478,
//        2.6552487856081397,
//        -0.5426487422908495,
//        -5.924500452373084,
//        -1.0468679968084609,
//        -1.5161574391336945,
//        2.7839905961268943,
//        5.484070242739277,
//        -4.873996579213912,
//        -6.092233471077399,
//        -0.022001707275093017,
//        0.564459437950626,
//        0.907122518248299,
//        -6.553027427227634,
//        -6.009507710226025,
//        -0.9020021757072173,
//        1.494470179080614,
//        -9.165549211050841,
//        -3.6131050298934224,
//        2.573803583328586,
//        2.3465609471901243,
//        -0.7812663038597119,
//        -6.125691254438041,
//        -3.5052234596611984,
//        -6.417271963900927,
//        -1.4726593932127692,
//        4.648915343084241,
//        1.8029481897816824,
//        3.7014674576082225,
//        -3.714625863753748,
//        -4.141391802900757,
//        8.390267621093182,
//        -6.765110152139823,
//        -1.324653642161031,
//        -5.252262746017095,
//        5.096039237604227,
//        -0.210854691246696,
//        -5.995955270882948,
//        -7.845495435930527,
//        5.176421428382265,
//        6.171107392579245,
//        4.8405022269087485,
//        -2.261117140082597,
//        6.965437608206508,
//        3.027558051511808,
//        -3.9046028477524377,
//        -6.568220155646815,
//        -8.53904203363471,
//        -5.821608924770603,
//        3.820509616933265,
//        -0.292679752628914,
//        -4.524829182464805,
//        6.978161060729645,
//        -1.853683278376152,
//        -1.9258264024707488,
//        -2.6049673583390724,
//        -7.9678864133124865,
//        -2.790005610137235,
//        3.7988517645062743,
//        -5.745667539031746,
//        1.6603768192051656,
//        -2.324753602105591,
//        -9.02658403664996,
//        2.875466216305487,
//        7.078361953808582,
//        4.642367140735055,
//        6.375165453357,
//        2.5849424610404017,
//        6.444553318432222,
//        -3.568939216856798,
//        2.1665882751942362,
//        -5.774124579726432,
//        2.587163359099775,
//        -4.209104238259954,
//        3.6597362159720044,
//        -3.137811211127537,
//        -5.6111793361144855,
//        5.505076235825238,
//        -9.35670050298312,
//        -4.590956681055973,
//        -4.526071125023478,
//        2.791678345248631,
//        4.475827155973454,
//        -2.0444899622831727,
//        0.9403995161930814,
//        7.723852804681119,
//        5.692754393297005,
//        -5.275224139740908,
//        5.764584098357495,
//        2.4877122972889523,
//        -3.141027880161663,
//        -2.9169716514488035,
//        -1.8044420965658943,
//        5.981986969126928,
//        7.33868689609001,
//        -3.2300588861928006,
//        -2.9347680938979406,
//        -3.2901523078696995,
//        -4.363008643235696,
//        -6.666035775083519,
//        -4.866591509454909,
//        -6.799214166674607,
//        -3.491649836547341,
//        5.790343752400695,
//        0.16889499402009442,
//        -9.850685653898768,
//        -7.760511916233757,
//        3.328445186146906,
//        -5.69232742831348,
//        -2.073386757508784,
//        -1.2656046136802888,
//        7.634479501081388,
//        7.743071295116402,
//        -1.829273049657999,
//        -8.46662539788253,
//        -4.46877942977761,
//        -0.4347893319477201,
//        0.061816317737444164,
//        -6.276576143946435,
//        -4.373596481424011,
//        2.552023214516353,
//        -2.4919383942841216,
//        6.398204198425364,
//        1.8591986790530386,
//        5.995964527529728,
//        7.234698621420275,
//        6.445228858738831,
//        -2.331483379671995,
//        -2.8412292365030813,
//        -4.278504596211142,
//        -6.6017428911169995,
//        2.0907094610066634,
//        6.597454113227639,
//        -2.438297396461457,
//        -0.769550594502457,
//        4.658996058738305,
//        -4.099281149942382,
//        5.544750606284057,
//        3.609101627363689,
//        -9.219709106022169,
//        -4.833248648340951,
//        3.616675521862225,
//        -3.3104905124505795,
//        -2.600791614335546,
//        -4.934946365883676,
//        5.752283994867363,
//        1.151933176445008,
//        2.8006944443217034,
//        4.410386390863371,
//        -1.2123901350432489,
//        0.8547742431036339,
//        3.250788760551965,
//        7.6250342240256295,
//        -9.14486107558933,
//        -7.783218541646377,
//        -3.3217157483877555,
//        2.756100292137269,
//        1.0072838141275204,
//        3.737985007032164,
//        8.125534412409325,
//        4.370858036672544,
//        7.77469683588292,
//        0.9292183781377892,
//        -5.20186064471196,
//        -8.611761344751299,
//        5.5788579396466345,
//        2.3111220602888864,
//        -5.1003623814742065,
//        -1.1805596423473566,
//        3.2152556667732486,
//        -1.8880057437125244,
//        6.080158600282252,
//        2.24269116966442,
//        5.572356695516909,
//        -3.5482608225717596,
//        5.490774359047769,
//        -3.8547699231575083,
//        -5.874881864215049,
//        -3.2809542373316067,
//        4.607621120848345,
//        -4.170758562281014,
//        0.33001845518128314,
//        3.7055469576835143,
//        6.61606182371224,
//        -4.668902381374866,
//        -1.3330259324042923,
//        -2.6048247995328055,
//        3.710022516137458,
//        -1.3385303708997442,
//        -5.325579431427357,
//        3.5426983947323305,
//        -4.051829220054191,
//        7.084065832740022,
//        -5.533009187020089,
//        5.845735008948429,
//        5.582473741090823,
//        -6.760745490870277,
//        3.1178672368718487,
//        6.865048625980365,
//        -5.485573211041139,
//        6.089273465919885,
//        -5.805905488636899,
//        1.4695018254403807,
//        1.9236816305076823,
//        4.930493711209329};
//
//    private final int[] seed2 = {174, 385, 38, 377, 187, 393, 68, 283, 104, 317, 19, 128, 203, 268, 97, 327, 228, //188, 235, 365,
//        288, 172, 81, 364, 157, 155, 230, 3, 205, 399, 330, 279, 177, 291, 229, 221, 304, 319, 13, 239,
//        154, 300, 121, 285, 73, 17, 99, 126, 59, 276, 189, 176, 261, 65, 123, 278, 138, 179, 272, 224,
//        329, 219, 287, 378, 360, 337, 92, 27, 61, 86, 289, 12, 246, 386, 250, 242, 335, 248, 180, 156,
//        93, 89, 162, 320, 78, 1, 345, 129, 290, 95, 39, 263, 151, 170, 341, 286, 167, 234, 342, 223,
//        351, 117, 46, 44, 173, 116, 244, 115, 395, 58, 326, 130, 298, 15, 220, 169, 74, 36, 376, 216,
//        280, 383, 251, 328, 284, 204, 193, 373, 83, 106, 4, 168, 255, 79, 122, 380, 32, 314, 296, 356,
//        310, 270, 62, 28, 372, 357, 105, 226, 331, 40, 240, 359, 94, 225, 206, 324, 375, 103, 343, 211,
//        396, 113, 355, 47, 145, 334, 388, 371, 102, 140, 50, 353, 201, 56, 217, 54, 197, 7, 249, 318, 64,
//        271, 369, 311, 25, 142, 2, 57, 127, 111, 133, 144, 215, 275, 70, 184, 159, 350, 308, 80, 241, 294,
//        100, 232, 398, 316, 309, 52, 236, 397, 141, 231, 87, 218, 361, 306, 76, 55, 45, 135, 254, 72, 120,
//        392, 69, 209, 273, 183, 266, 391, 389, 10, 192, 26, 293, 190, 20, 8, 332, 182, 297, 336, 208, 281,
//        200, 137, 390, 258, 346, 146, 175, 71, 132, 212, 90, 84, 367, 16, 24, 227, 14, 382, 245, 259, 148,
//        292, 313, 352, 101, 131, 139, 267, 82, 9, 18, 256, 237, 257, 147, 143, 134, 384, 282, 260, 315,
//        363, 185, 178, 63, 6, 35, 199, 37, 379, 163, 196, 11, 362, 124, 348, 34, 30, 31, 368, 358, 171,
//        75, 374, 322, 274, 60, 158, 152, 195, 21, 96, 161, 301, 5, 233, 194, 108, 77, 238, 264, 354, 339,
//        191, 349, 149, 243, 295, 198, 109, 42, 277, 222, 125, 186, 325, 312, 51, 303, 136, 41, 118, 370,
//        67, 299, 394, 110, 321, 213, 23, 53, 166, 381, 307, 340, 112, 160, 202, 119, 214, 344, 98, 107,
//        164, 29, 269, 302, 323, 366, 338, 114, 66, 265, 247, 91, 48, 165, 0, 333, 88, 43, 153, 49, 85,
//        150, 305, 207, 347, 210, 253, 262, 33, 387, 252, 22, 181};
//    
//}