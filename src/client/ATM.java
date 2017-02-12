package client;

import exceptions.*;
import interfaces.BankInterface;
import server.Account;
import server.Statement;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ATM {
    static int serverAddress, serverPort, account, amount;
    static String operation, username, password;
    static long sessionID, id=0;
    static BankInterface bank;
    static Date startDate, endDate;


    public static void main (String args[]) {
        try {
            getCommandLineArguments(args);
            String name = "Bank";
            Registry registry = LocateRegistry.getRegistry(serverPort);
            bank = (BankInterface) registry.lookup(name);
            System.out.println("\n----------------\nClient Connected" + "\n----------------\n");
        } catch (InvalidArgumentException ie){
            ie.printStackTrace();
            System.out.println(ie);
        } catch (Exception e){
            e.printStackTrace();
            System.out.println(e);
        }
        double balance;
        switch (operation){
            case "login":
                try {
                    id = bank.login(username, password);
                    Account acc = bank.accountDetails(id);
                    System.out.println("--------------------------\nAccount Details:\n--------------------------\n" +
                                       "Account Number: " + acc.getAccountNumber() +
                                       "\nSessionID: " + id +
                                       "\nUsername: " + acc.getUserName() +
                                       "\nBalance: " + acc.getBalance() +
                                       "\n--------------------------\n");
                    System.out.println("Use SessionID " + id + " for all other operations");
                } catch (RemoteException e) {
                    e.printStackTrace();
                } catch (InvalidLoginException e) {
                    e.printStackTrace();
                } catch (InvalidSessionException e) {
                    e.printStackTrace();
                }
                break;
            case "deposit":
                try {
                    balance = bank.deposit(account, amount, sessionID);
                    System.out.println("Successfully deposited E" + amount + " into account " + account);
                    System.out.println("New balance: E" + balance);
                } catch (RemoteException e) {
                    e.printStackTrace();
                } catch (InvalidSessionException e) {
                    System.out.println(e.getMessage());
                }
                break;
            case "withdraw":
                try {
                    balance = bank.withdraw(account, amount, sessionID);
                    System.out.println("Successfully withdrew E" + amount + " from account " + account +
                                       "\nRemaining Balance: E" + balance);
                } catch (RemoteException e) {
                    e.printStackTrace();
                } catch (InvalidSessionException e) {
                    System.out.println(e.getMessage());
                } catch (InsufficientFundsException e) {
                    System.out.println(e.getMessage());
                }
                break;
            case "inquiry":
                try {
                    Account acc = bank.inquiry(account,sessionID);
                    System.out.println("--------------------------\nAccount Details:\n--------------------------\n" +
                            "Account Number: " + acc.getAccountNumber() +
                            "\nUsername: " + acc.getUserName() +
                            "\nBalance: E" + acc.getBalance() +
                            "\n--------------------------\n");
                } catch (RemoteException e) {
                    e.printStackTrace();
                } catch (InvalidSessionException e) {
                    System.out.println(e.getMessage());
                }
                break;
            case "statement":
                Statement s = null;
                try {
                    s = (Statement) bank.getStatement(account, startDate, endDate, sessionID);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                    System.out.print("-----------------------------------------------------------------------\n");
                    System.out.println("Statement for Account " + account + " between " +
                                       dateFormat.format(startDate) + " and " + dateFormat.format(endDate));
                    System.out.print("-----------------------------------------------------------------------\n");
                    System.out.println("Date\t\t\tTransaction Type\tAmount\t\tBalance");
                    System.out.print("-----------------------------------------------------------------------\n");
                    for(Object t : s.getTransations()) {
                        System.out.println(t);
                    }
                    System.out.print("-----------------------------------------------------------------------\n");
                } catch (RemoteException e) {
                    e.printStackTrace();
                } catch (InvalidSessionException e) {
                    System.out.println(e.getMessage());
                } catch (StatementException e) {
                    System.out.println(e.getMessage());
                }
                break;
            default:
                System.out.println("Operation not supported");
                break;
        }
    }

    public static void getCommandLineArguments(String args[]) throws InvalidArgumentException{
        if(args.length < 4) {
            throw new InvalidArgumentException();
        }
        serverPort = Integer.parseInt(args[1]);
        operation = args[2];
        switch (operation){
            case "login":
                username = args[3];
                password = args[4];
                break;
            case "withdraw":
            case "deposit":
                amount = Integer.parseInt(args[4]);
                account = Integer.parseInt(args[3]);
                sessionID = Long.parseLong(args[5]);
                break;
            case "inquiry":
                account = Integer.parseInt(args[3]);
                sessionID = Long.parseLong(args[4]);
                break;
            case "statement":
                account = Integer.parseInt(args[3]);
                startDate = new Date(args[4]);
                endDate = new Date(args[5]);
                sessionID = Long.parseLong(args[6]);
                break;
        }
    }
}