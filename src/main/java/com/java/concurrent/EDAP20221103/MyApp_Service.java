//package com.java.concurrent.EDAP20221103;
//
//import javafx.application.Application;
//import javafx.application.Platform;
//import javafx.beans.value.ChangeListener;
//import javafx.beans.value.ObservableValue;
//import javafx.concurrent.Service;
//import javafx.concurrent.Task;
//import javafx.concurrent.Worker;
//import javafx.scene.Scene;
//import javafx.scene.control.Button;
//import javafx.scene.control.Label;
//import javafx.scene.control.ProgressBar;
//import javafx.scene.layout.FlowPane;
//import javafx.scene.layout.HBox;
//import javafx.stage.Stage;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//
///**
// * @author stone
// */
//
//public class MyApp_Service extends Application {
//
//    @Override
//    public void start(Stage primaryStage) throws Exception {
//        primaryStage.setTitle("Hello World!");
//
//        HBox hBox = new HBox(20);
//
//        Button startBtn = new Button("开始");
//        Button cancelBtn = new Button("取消");
//        Button resetBtn = new Button("重置");
//        Button restartBtn = new Button("重启");
//        ProgressBar progressBar = new ProgressBar(0);
//        progressBar.setPrefWidth(200);
//
//        Label l1 = new Label("state");
//        Label l2 = new Label("value");
//        Label l3 = new Label("title");
//        Label l4 = new Label("message");
//
//        FlowPane root = new FlowPane();
//        hBox.getChildren().addAll(startBtn, cancelBtn, restartBtn, resetBtn, progressBar, l1, l2, l3, l4);
//        root.getChildren().add(hBox);
//        primaryStage.setScene(new Scene(root, 700, 300));
//        primaryStage.show();
//
//        MyService ms = new MyService();
//
//        startBtn.setOnAction(event ->
//                ms.start()
//        );
//        cancelBtn.setOnAction(event ->
//                ms.cancel()
//        );
//        resetBtn.setOnAction(event -> {
//            ms.reset();
//            progressBar.setProgress(0);
//        });
//        restartBtn.setOnAction(event -> {
//            ms.restart();
//        });
//
//
//        ms.progressProperty().addListener(new ChangeListener<Number>() {
//            @Override
//            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
//                progressBar.setProgress(newValue.doubleValue());
//            }
//        });
//
//        ms.titleProperty().addListener(new ChangeListener<String>() {
//            @Override
//            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
//                l3.setText(newValue);
//            }
//        });
//
//        ms.valueProperty().addListener(new ChangeListener<Number>() {
//            @Override
//            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
//                if (newValue.doubleValue() == 1) {
//                    l2.setText("完成");
//                }
//            }
//        });
//
//        ms.messageProperty().addListener(new ChangeListener<String>() {
//            @Override
//            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
//                l4.setText(newValue);
//            }
//        });
//
//        //wordkDone指的是进度
//        ms.stateProperty().addListener(new ChangeListener<Worker.State>() {
//            @Override
//            public void changed(ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue) {
//                l1.setText(newValue.toString());
//            }
//        });
//
//        //异常监听 监听现在状态是否有异常并打印
//        ms.exceptionProperty().addListener(new ChangeListener<Throwable>() {
//            @Override
//            public void changed(ObservableValue<? extends Throwable> observable, Throwable oldValue, Throwable newValue) {
//                System.out.println(newValue);
//            }
//        });
//
//    }
//
//    public static void main(String[] args) {
//        launch(args);
//    }
//}
//
//
//class MyService extends Service<Number> {
//
//    @Override
//    protected void executeTask(Task<Number> task) {
//        super.executeTask(task);
//        task.valueProperty().addListener(new ChangeListener<Number>() {
//            @Override
//            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
//                System.out.println("executeTask valueProperty");
//            }
//        });
//    }
//
//    @Override
//    protected void ready() {
//        super.ready();
//        System.out.println("ready" + Platform.isFxApplicationThread());
//    }
//
//    @Override
//    protected void scheduled() {
//        super.scheduled();
//        System.out.println("scheduled " + Platform.isFxApplicationThread());
//    }
//
//    @Override
//    protected void running() {
//        super.running();
//        System.out.println("running " + Platform.isFxApplicationThread());
//    }
//
//    @Override
//    protected void succeeded() {
//        super.succeeded();
//        System.out.println("succeeded " + Platform.isFxApplicationThread());
//    }
//
//    @Override
//    protected void cancelled() {
//        super.cancelled();
//        System.out.println("cancelled " + Platform.isFxApplicationThread());
//    }
//
//    @Override
//    protected void failed() {
//        super.failed();
//        System.out.println("failed " + Platform.isFxApplicationThread());
//    }
//
//    @Override
//    protected Task<Number> createTask() {
//
//        Task<Number> task = new Task<Number>() {
//            @Override
//            protected Number call() throws Exception {
//
//                this.updateTitle("copy");
//                FileInputStream fis = new FileInputStream(new File("C:\\Users\\HJN13\\Desktop\\黄江南-请假.docx"));
//                FileOutputStream fos = new FileOutputStream(new File("C:\\Users\\HJN13\\Desktop\\西北工业大学 横线16开.docx"));
//                //获取字节长
//                double max = fis.available();
//                byte[] readbyte = new byte[10000];
//                int i = 0;
//                //目前完成进度
//                double sum = 0;
//                //进度
//                double progress = 0;
//                while ((i = fis.read(readbyte, 0, readbyte.length)) != -1) {
//
//                    /*if (this.isCancelled()){
//                        break;
//                    }*/
//                    fos.write(readbyte, 0, i);
//                    sum = sum + i;
//                    //当前大小和总共大小
//                    this.updateProgress(sum, max);
//                    progress = sum / max;
//                    /*System.out.println(progress);*/
//                    Thread.sleep(50);
//                    if (progress < 0.5) {
//                        this.updateMessage("请耐心等待");
//                    } else if (progress < 0.8) {
//                        this.updateMessage("马上就好");
//                    } else if (progress < 1) {
//                        this.updateMessage("即将完成");
//                    } else if (progress >= 1) {
//                        this.updateMessage("搞定了");
//                    }
//                }
//
//                fis.close();
//                fos.close();
//                System.out.println(Platform.isFxApplicationThread());
//                return progress;
//            }
//        };
//        return task;
//    }
//}
