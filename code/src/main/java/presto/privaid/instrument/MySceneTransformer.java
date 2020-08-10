package presto.privaid.instrument;

import soot.*;
import soot.jimple.*;

import java.util.*;

public class MySceneTransformer extends SceneTransformer {
    final static String TAG = MySceneTransformer.class.getSimpleName();
    private Map <String, DictItemExtractor> dictItemExtractorMap = new HashMap<>();

    interface DictItemExtractor {
        List<Stmt> extractDictItem(Local obj, Local strVar, Body body);

        DictItemExtractor infowarsDictItemExtractor = new DictItemExtractor() {
            @Override
            public List<Stmt> extractDictItem(Local obj, Local strVar, Body body) {
                List<Stmt> stmts = new ArrayList<>();

                // String articleUrl = article.getPermalink()
                SootClass articleClass = Scene.v().getSootClass("com.infowars.official.model.Article");
                Value assignStmtRhs = Jimple.v().newVirtualInvokeExpr(
                        obj,
                        articleClass.getMethod("java.lang.String getPermalink()").makeRef());
                stmts.add(Jimple.v().newAssignStmt(strVar, assignStmtRhs));

                return stmts;
            }
        };

        DictItemExtractor cookbookDictItemExtractor = new DictItemExtractor() {
            @Override
            public List<Stmt> extractDictItem(Local obj, Local strVar, Body body) {
                List<Stmt> stmts = new ArrayList<>();

                // String recipeId = Long.toString(lRecipeId)
                Value assignStmtRhs = Jimple.v().newStaticInvokeExpr(
                        Scene.v().getSootClass("java.lang.Long")
                                .getMethod("java.lang.String toString(long)").makeRef(),
                        obj);
                stmts.add(Jimple.v().newAssignStmt(strVar, assignStmtRhs));

                return stmts;
            }
        };

        DictItemExtractor loopDictItemExtractor = new DictItemExtractor() {
            @Override
            public List<Stmt> extractDictItem(Local obj, Local strVar, Body body) {
                List<Stmt> stmts = new ArrayList<>();

                // String articleId = article.getTitle()
                SootClass itemClass = Scene.v().getSootClass("com.aggrego.loop.model.LatestModel");
                Value assignStmtRhs = Jimple.v().newVirtualInvokeExpr(
                        obj,
                        itemClass.getMethod("java.lang.String getTitle()").makeRef());
                stmts.add(Jimple.v().newAssignStmt(strVar, assignStmtRhs));

                return stmts;
            }
        };

        DictItemExtractor reststopsDictItemExtractor = new DictItemExtractor() {
            @Override
            public List<Stmt> extractDictItem(Local obj, Local strVar, Body body) {
                List<Stmt> stmts = new ArrayList<>();

                // String itemId = item.getObjectId()
                SootClass reststopClass = Scene.v().getSootClass("com.insofttech.reststops.b.b");
                Value assignStmtRhs = Jimple.v().newVirtualInvokeExpr(
                        obj,
                        reststopClass.getSuperclass().getMethod("java.lang.String getObjectId()").makeRef());
                stmts.add(Jimple.v().newAssignStmt(strVar, assignStmtRhs));

                return stmts;
            }
        };

        DictItemExtractor opensnowDictItemExtractor = new DictItemExtractor() {
            @Override
            public List<Stmt> extractDictItem(Local obj, Local strVar, Body body) {
                List<Stmt> stmts = new ArrayList<>();

                // String newsId = Long.toString(lNewsId)
                Value assignStmtRhs = Jimple.v().newStaticInvokeExpr(
                        Scene.v().getSootClass("java.lang.Long")
                                .getMethod("java.lang.String toString(long)").makeRef(),
                        obj);
                stmts.add(Jimple.v().newAssignStmt(strVar, assignStmtRhs));

                return stmts;
            }
        };

        DictItemExtractor shipmateDictItemExtractor = new DictItemExtractor() {
            @Override
            public List<Stmt> extractDictItem(Local obj, Local strVar, Body body) {
                List<Stmt> stmts = new ArrayList<>();

                Local photoIdLong = Jimple.v().newLocal("photoIdLong", Scene.v().getType("long"));
                body.getLocals().add(photoIdLong);

                // long photoIdLong = item.getPhotoId();
                SootClass photoItemClass = Scene.v().getSootClass("shipmate.carnival.model.database.PhotoListItem");
                Value assignLongIdRhs = Jimple.v().newVirtualInvokeExpr(
                        obj,
                        photoItemClass.getMethod("long getPhotoId()").makeRef());
                stmts.add(Jimple.v().newAssignStmt(photoIdLong, assignLongIdRhs));

                // String photoId = Long.toString(photoLongId);
                Value assignStmtRhs = Jimple.v().newStaticInvokeExpr(
                        Scene.v().getSootClass("java.lang.Long")
                                .getMethod("java.lang.String toString(long)").makeRef(),
                        photoIdLong);
                stmts.add(Jimple.v().newAssignStmt(strVar, assignStmtRhs));

                return stmts;
            }
        };

        DictItemExtractor channelsDictItemExtractor = new DictItemExtractor() {
            @Override
            public List<Stmt> extractDictItem(Local obj, Local strVar, Body body) {
                List<Stmt> stmts = new ArrayList<>();

                // String postUrl = <obj>;
                stmts.add(Jimple.v().newAssignStmt(strVar, obj));

                return stmts;
            }
        };

        DictItemExtractor apartmentguideDictItemExtractor = new DictItemExtractor() {
            @Override
            public List<Stmt> extractDictItem(Local obj, Local strVar, Body body) {
                List<Stmt> stmts = new ArrayList<>();

                // String inventoryId = item.getId()
                SootClass inventoryClass = Scene.v().getSootClass("com.rentpath.networking.model.RPInventory");
                Value assignStmtRhs = Jimple.v().newVirtualInvokeExpr(
                        obj,
                        inventoryClass.getMethod("java.lang.String getId()").makeRef());
                stmts.add(Jimple.v().newAssignStmt(strVar, assignStmtRhs));

                return stmts;
            }
        };

        DictItemExtractor rentDictItemExtractor = apartmentguideDictItemExtractor;

        DictItemExtractor androidauthorityDictItemExtractor = new DictItemExtractor() {
            @Override
            public List<Stmt> extractDictItem(Local obj, Local strVar, Body body) {
                List<Stmt> stmts = new ArrayList<>();

                Local postIdLong = Jimple.v().newLocal("postIdLong", Scene.v().getType("java.lang.Long"));
                body.getLocals().add(postIdLong);

                // Long postIdLong = item.getId()
                SootClass postClass = Scene.v().getSootClass("com.androidauthority.app.model.Post");
                Value assignLongIdRhs = Jimple.v().newVirtualInvokeExpr(
                        obj,
                        postClass.getSuperclass().getMethod("java.lang.Long getId()").makeRef());
                stmts.add(Jimple.v().newAssignStmt(postIdLong, assignLongIdRhs));

                // String postId = postIdLong.toString()
                Value assignStmtRhs = Jimple.v().newVirtualInvokeExpr(postIdLong,
                        Scene.v().getSootClass("java.lang.Long")
                                .getMethod("java.lang.String toString()").makeRef());
                stmts.add(Jimple.v().newAssignStmt(strVar, assignStmtRhs));

                return stmts;
            }
        };
    }

    private void initTrackerAndDb() {
        SootClass trackerClass = Scene.v().getSootClass("presto.privaid.Tracker");
        String trackerGetInstanceSubsig = "presto.privaid.Tracker getInstance(android.content.Context)";
        SootClass dbControllerClass = Scene.v().getSootClass("presto.privaid.DatabaseController");
        String dbcGetInstanceSubsig = "presto.privaid.DatabaseController getInstance(android.content.Context)";

        Map map = (Map) Configs.appInstrumentSpec.get("init");
        SootClass cls = Scene.v().getSootClass((String) map.get("className"));
        SootMethod method = cls.getMethod((String) map.get("method"));
        Body body = method.retrieveActiveBody();
        Logger.info(TAG, ">>> Instrumenting: " + method.getSignature());

        Stmt faInitStmt = Jimple.v().newInvokeStmt(
                Jimple.v().newStaticInvokeExpr(trackerClass.getMethod(trackerGetInstanceSubsig).makeRef(), body.getThisLocal()));
        Stmt dbcInitStmt = Jimple.v().newInvokeStmt(
                Jimple.v().newStaticInvokeExpr(dbControllerClass.getMethod(dbcGetInstanceSubsig).makeRef(), body.getThisLocal()));

        int nParam = method.getParameterCount();
        if (!method.isStatic()) nParam++;

        if (nParam == 0) {
            body.getUnits().addFirst(faInitStmt);
            body.getUnits().addFirst(dbcInitStmt);
        } else {
            Iterator<Unit> iter = body.getUnits().snapshotIterator();
            Stmt stmt = null;
            for (int i = 0; i < nParam; i++) {
                stmt = (Stmt) iter.next();
            }
            body.getUnits().insertAfter(faInitStmt, stmt);
            body.getUnits().insertAfter(dbcInitStmt, stmt);
        }
        Logger.info(TAG, "Adding: " + faInitStmt);
        Logger.info(TAG, "Adding: " + dbcInitStmt);
    }

    private void replaceLogEventCall() {
        List list = (List) Configs.appInstrumentSpec.get("replaceLogEventCall");

        for (Object item : list) {
            Map map = (Map) item;
            SootClass cls = Scene.v().getSootClass((String) map.get("className"));
            String mtdSubSig = (String) map.get("method");
            SootClass trackerClass = Scene.v().getSootClass("presto.privaid.Tracker");
            String trackerLogEventSubsig = "void logEvent(java.lang.String,android.os.Bundle)";

            SootMethod mtd = cls.getMethod(mtdSubSig);
            Body body = mtd.retrieveActiveBody();
            Logger.info(TAG, ">>> Instrumenting: " + mtd.getSignature());

            for (final Iterator<Unit> iter = body.getUnits().snapshotIterator(); iter.hasNext(); ) {
                Stmt stmt = (Stmt) iter.next();
                if (stmt.containsInvokeExpr() && stmt.getInvokeExpr().getMethod().getSignature().equals(
                        "<com.google.firebase.analytics.FirebaseAnalytics: void logEvent(java.lang.String,android.os.Bundle)>")) {
                    Logger.info(TAG, "Replacing call to logEvent: " + stmt);

                    Local tracker = Jimple.v().newLocal("privaidTracker", trackerClass.getType());
                    body.getLocals().addLast(tracker);
                    Stmt getInstance = Jimple.v().newAssignStmt(tracker,
                            Jimple.v().newStaticInvokeExpr(trackerClass.getMethod("presto.privaid.Tracker getInstance()").makeRef()));

                    Stmt trackerLogEvent = Jimple.v().newInvokeStmt(
                            Jimple.v().newVirtualInvokeExpr(tracker, trackerClass.getMethod(trackerLogEventSubsig).makeRef(), stmt.getInvokeExpr().getArgs()));

                    Logger.info(TAG, "Replacing with: " + getInstance);
                    Logger.info(TAG, "Followed by: " + trackerLogEvent);
                    body.getUnits().insertAfter(trackerLogEvent, stmt);
                    body.getUnits().insertAfter(getInstance, stmt);
                    body.getUnits().remove(stmt);
                    break;
                }
            }
        }
    }

    private void addContentToDictionary() {
        List list = (List) Configs.appInstrumentSpec.get("addContentToDictionary");

        for (Object item : list) {
            Map map = (Map) item;
            if (map.containsKey("collection") && (Boolean) map.get("collection")) {
                SootClass dictManCls = Scene.v().getSootClass("presto.privaid.firebase.ContentDictionaryManager");
                SootMethod addAllMtd = dictManCls.getMethod("void addAllContents(java.lang.String,java.lang.String,java.util.Collection)");
                SootMethod method = Scene.v().getSootClass((String) map.get("className")).getMethod((String) map.get("method"));
                Stmt stmt = null;

                // instrument the methods that adds multiple elements to list
                Body body = method.retrieveActiveBody();
                for (final Iterator<Unit> iter = body.getUnits().snapshotIterator(); iter.hasNext(); ) {
                    stmt = (Stmt) iter.next();
                    if (!stmt.toString().equals((String) map.get("insertAfter"))) {
                        continue;
                    }
                    Logger.info(TAG, ">>> Instrumenting: " + method.getSignature());
                    Logger.info(TAG, "After: " + stmt);

                    // find the parameter (of type collection) passed to methods like addAll
                    InvokeExpr callExpr = ((InvokeStmt) stmt).getInvokeExpr();
                    int n = ((Double) map.get("paramN")).intValue();
                    Local collectionLocal = (Local) callExpr.getArgs().get(n);
                    InvokeStmt addAllStmt = Jimple.v().newInvokeStmt(
                            Jimple.v().newStaticInvokeExpr(addAllMtd.makeRef(), StringConstant.v((String) map.get("eventType")),
                                    StringConstant.v((String) map.get("bundleKey")), collectionLocal));
                    Logger.info(TAG, "" + addAllStmt);
                    body.getUnits().insertAfter(addAllStmt, stmt);
                }

                // instrument ContentDictionaryManager.addAllContents method
                body = addAllMtd.retrieveActiveBody();
                for (final Iterator<Unit> iter = body.getUnits().snapshotIterator(); iter.hasNext(); ) {
                    stmt = (Stmt) iter.next();
                    if (!stmt.toString().equals("r1 = r0")) {
                        continue;
                    }
                    Logger.info(TAG, ">>> Instrumenting: " + addAllMtd.getSignature());
                    Logger.info(TAG, "After: " + stmt);

                    // cast the collection item to appropriate class to extract id
                    Local localObj = (Local) ((AssignStmt) stmt).getRightOp();
                    SootClass castCls = Scene.v().getSootClass((String) map.get("castTo"));
                    Local castObj = Jimple.v().newLocal("castObj", castCls.getType());
                    body.getLocals().add(castObj);
                    Stmt castStmt = Jimple.v().newAssignStmt(castObj, Jimple.v().newCastExpr(localObj, castCls.getType()));
                    Logger.info(TAG, "" + castStmt);
                    body.getUnits().insertAfter(castStmt, stmt);
                    map.put("insertAfter", castStmt.toString());
                }
                map.put("className", "presto.privaid.firebase.ContentDictionaryManager");
                map.put("method", "void addAllContents(java.lang.String,java.lang.String,java.util.Collection)");
                map.put("collection", "false");
            }

            addContentToDict(map);
        }
    }

    private void addContentToDict(Map map) {
        SootClass cls = Scene.v().getSootClass((String) map.get("className"));
        String mtdSubSig = (String) map.get("method");
        SootMethod mtd = cls.getMethod(mtdSubSig);
        Body body = mtd.retrieveActiveBody();
        String insertAfter = (String) map.get("insertAfter");

        Stmt stmt = null;
        for (final Iterator<Unit> iter = body.getUnits().snapshotIterator(); iter.hasNext(); ) {
            stmt = (Stmt) iter.next();
            if (!stmt.toString().equals(insertAfter)) {
                continue;
            }
            Logger.info(TAG, ">>> Instrumenting: " + mtd.getSignature());
            Logger.info(TAG, "After: " + stmt);

            List<Stmt> toInsert = new ArrayList<>();

            // create local String variable to store dictionary element
            Local strVar = Jimple.v().newLocal((String) map.get("stringVariableName"),
                    Scene.v().getSootClass("java.lang.String").getType());
            body.getLocals().addLast(strVar);

            // get local variable to extract the string from
            Local objLocal;
            if (stmt instanceof AssignStmt) {
                objLocal = (Local) ((AssignStmt) stmt).getLeftOp();
            } else if (stmt instanceof InvokeStmt) { // for constructors
                objLocal = (Local) ((SpecialInvokeExpr) stmt.getInvokeExpr()).getBase();
            } else {
                throw new RuntimeException("Could not find a local variable");
            }

            // find the method to extract id from local variable using reflection
            DictItemExtractor dictItemExtractor = null;
            try {
                dictItemExtractor = (DictItemExtractor) DictItemExtractor.class.getField((String) map.get("extractorMethod")).get(this);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }

            if (dictItemExtractor == null) {
                throw new RuntimeException("DictItemExtractor method not defined");
            }

            List<Stmt> extract = dictItemExtractor.extractDictItem(objLocal, strVar, body);
            for (Stmt s : extract) {
                Logger.info(TAG, "" + s);
            }
            toInsert.addAll(extract);

            // Bundle bundle = new Bundle()
            SootClass bundleClass = Scene.v().getSootClass("android.os.Bundle");
            Local bundle = Jimple.v().newLocal("bundle", bundleClass.getType());
            body.getLocals().addLast(bundle);
            Stmt newBundle = Jimple.v().newAssignStmt(bundle, Jimple.v().newNewExpr(bundleClass.getType()));
            Logger.info(TAG, "" + newBundle);
            toInsert.add(newBundle);
            Stmt bundleConstr = Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(bundle,
                    bundleClass.getMethod("void <init>()").makeRef()));
            Logger.info(TAG, "" + bundleConstr);
            toInsert.add(bundleConstr);

            // put local string into bundle
            Stmt bundlePutValue = Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(bundle,
                    bundleClass.getSuperclass().getMethod("void putString(java.lang.String,java.lang.String)").makeRef(),
                    StringConstant.v((String) map.get("bundleKey")), strVar));
            Logger.info(TAG, "" + bundlePutValue);
            toInsert.add(bundlePutValue);

            // ContentDictionaryManager.addContent(<event type>, bundle)
            SootClass dictClass = Scene.v().getSootClass("presto.privaid.firebase.ContentDictionaryManager");
            Stmt addToDict = Jimple.v().newInvokeStmt(
                    Jimple.v().newStaticInvokeExpr(dictClass.getMethod("void addContent(java.lang.String,android.os.Bundle)").makeRef(),
                            StringConstant.v((String) map.get("eventType")), bundle));
            Logger.info(TAG, "" + addToDict);
            toInsert.add(addToDict);

            body.getUnits().insertAfter(toInsert, stmt);
        }
    }

    private void printStats() {
        int nClass = 0;
        int nMethod = 0;

        Logger.info(TAG, "#LibPackages: " + Configs.excludePackages.size());

        for (SootClass cls : Scene.v().getApplicationClasses()) {
            boolean isLib = false;
            for (String pkg : Configs.excludePackages) {
                if (cls.getName().startsWith(pkg)) {
                    isLib = true;
                }
            }
            if (!isLib) {
                nClass++;
                nMethod += cls.getMethodCount();
            }
        }

        Logger.info(TAG, "#Class: " + nClass);
        Logger.info(TAG, "#Method: " + nMethod);
    }

    @Override
    protected void internalTransform(String phaseName, Map<String, String> options) {
        printStats();
        initTrackerAndDb();
        replaceLogEventCall();
        addContentToDictionary();
    }
}
