package com.java.listener;

public class MyRobotListener implements RobotListener{
    @Override
    public void working(Even even) {
        Robot robot = even.getRobot();
        System.out.println("机器人工作提示：请看管好你的机器人，防止它偷懒");
    }

    @Override
    public void dancing(Even even) {
        Robot robot = even.getRobot();
        System.out.println("机器人跳舞提示：机器人跳舞动作优美，请不要走神哦");
    }
}
