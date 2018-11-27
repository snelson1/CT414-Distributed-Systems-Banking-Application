//package client;
//
//import exceptions.*;
//import interfaces.BankInterface;
//import server.Account;
//import server.Statement;
//
//import java.rmi.NotBoundException;
//import java.rmi.RemoteException;
//import java.rmi.registry.LocateRegistry;
//import java.rmi.registry.Registry;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.Scanner;
//import java.util.Timer;
//import java.util.TimerTask;
//
//
//public class HeartbeatTask extends TimerTask {
//	private static BankInterface bank;
//	private static Timer heartbeatTimer;
//	private static int timerCount;
//
//
//	HeartbeatTask(BankInterface bank, Timer heartbeatTimer)
//	{
//		this.bank = bank;
//		this.heartbeatTimer = heartbeatTimer;
//	}
//
//	public void run() {
//		try{
//			boolean isAlive = bank.heartbeat();
//		}
//		catch(Exception e){
//			System.out.println("Server missing!");
//
//			timerCount++;
//
//
//			if (timerCount == 2) {
//				System.exit(0);
//			}
////			this.heartbeatTimer.cancel();
////			this.heartbeatTimer.purge();
////			return;
//			//System.exit(0);
//		}
//	}
//}