package client;

import exceptions.*;
import interfaces.BankInterface;
import server.Account;
import server.Statement;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

//Client program, which connects to the bank using RMI and class methods of the remote bank object
public class ATM {
    private static String serverAddress;
    private static int serverPort, account;
    private static String operation, username, password;
    private static long sessionID, id = 0;
    private static double amount;
    private static BankInterface bank;
    private static Date startDate, endDate;
    private static int timeoutCount = 0;
    private static Account acc;


    public static void main (String args[]) {
        System.out.println("client start");
        System.out.print(">> ");
        Scanner sc = new Scanner(System.in);
        while (sc.hasNextLine()) {
            String line = sc.nextLine().replaceAll("\n", "");
            //System.out.println(line);
            if (line.length() == 0) {
                continue;
            }

            String[] commands = line.split(" ");
            //System.out.println(commands);
            getCommandLineArguments(commands);
            double balance;

            //Switch based on the operation
            switch (operation){
                case "exit":
                    return;

                case "login":
                    try {
                        //Login with username and password
                        //Set up the rmi registry and get the remote bank object from it
                        String name = "Bank";
                        Registry registry = LocateRegistry.getRegistry(serverAddress, serverPort);
                        bank = (BankInterface) registry.lookup(name);
                        System.out.println("\n----------------\nClient Connected" + "\n----------------\n");

                        id = bank.login(username, password);
                        acc = bank.accountDetails(id);
                        sessionID = id;
                        account = acc.getAccountNumber();
                        //Print account details
                        System.out.println("--------------------------\nAccount Details:\n--------------------------\n" +
                                "Account Number: " + acc.getAccountNumber() +
                                "\nSessionID: " + id +
                                "\nUsername: " + acc.getUserName() +
                                "\nBalance: " + acc.getBalance() +
                                "\n--------------------------\n");
                        System.out.println("Session active for 5 minutes");
                        System.out.println("Use SessionID " + id + " for all other operations");
                        //Catch exceptions that can be thrown from the server
                    } catch (RemoteException e) {
                        System.out.println("connect lose");
                        //e.printStackTrace();
                    } catch (InvalidLoginException e) {
                        System.out.println("User name or password wrong");
                        //e.printStackTrace();
                    } catch (InvalidSessionException e) {
                        System.out.println("Please login again");
                        //e.printStackTrace();
                    } catch (NotBoundException e) {
                        e.printStackTrace();
                    }
                    break;

                case "deposit":
                    try {
                        //Make bank deposit and get updated balance
                        balance = bank.deposit(account, amount, sessionID);
                        System.out.println("Successfully deposited E" + amount + " into account " + account);
                        System.out.println("New balance: E" + balance);
                        //Catch exceptions that can be thrown from the server
                    } catch (RemoteException e) {
                        System.out.println("connect lose");
                        //e.printStackTrace();
                    } catch (InvalidSessionException e) {
                        System.out.println("Please login again");
                        //System.out.println(e.getMessage());
                    }
                    break;

                case "withdraw":
                    try {
                        //Make bank withdrawal and get updated balance
                        balance = bank.withdraw(account, amount, sessionID);
                        System.out.println("Successfully withdrew E" + amount + " from account " + account +
                                "\nRemaining Balance: E" + balance);
                        //Catch exceptions that can be thrown from the server
                    } catch (RemoteException e) {
                        System.out.println("connect lose");
                        //e.printStackTrace();
                    } catch (InvalidSessionException e) {
                        System.out.println("Please login again");
                        //System.out.println(e.getMessage());
                    } catch (InsufficientFundsException e) {
                        System.out.println("Not enough money");
                        //System.out.println(e.getMessage());
                    }
                    break;

                case "inquiry":
                    try {
                        //Get account details from bank
                        Account acc = bank.inquiry(account,sessionID);
                        System.out.println("--------------------------\nAccount Details:\n--------------------------\n" +
                                "Account Number: " + acc.getAccountNumber() +
                                "\nUsername: " + acc.getUserName() +
                                "\nBalance: E" + acc.getBalance() +
                                "\n--------------------------\n");
                        //Catch exceptions that can be thrown from the server
                    } catch (RemoteException e) {
                        System.out.println("connect lose");
                        //e.printStackTrace();
                    } catch (InvalidSessionException e) {
                        System.out.println("Please login again");
                        //System.out.println(e.getMessage());
                    }
                    break;

                case "statement":
                    Statement s = null;
                    try {
                        //Get statement for required dates
                        s = (Statement) bank.getStatement(account, startDate, endDate, sessionID);

                        //format statement for printing to the window
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
                        //Catch exceptions that can be thrown from the server
                    } catch (RemoteException e) {
                        System.out.println("connect lose");
                        //e.printStackTrace();
                    } catch (InvalidSessionException e) {
                        System.out.println("Please login again");
                        //System.out.println(e.getMessage());
                    } catch (StatementException e) {
                        System.out.println(e.getMessage());
                    }
                    break;

                default:
                    //Catch all case for operation that isn't one of the above
                    System.out.println("Operation not supported");
                    break;
            }
            System.out.print(">> ");
        }
    }



    public static void getCommandLineArguments(String args[]){
        //Makes sure server, port and operation are entered as arguments

//        if(args.length < 4) {
//            throw new InvalidArgumentException();
//        }

        //Parses arguments from command line
        //arguments are in different places based on operation, so switch needed here

        operation = args[0];

        switch (operation){
            case "exit":
                return;
            case "login":
                serverAddress = args[1];
                serverPort = Integer.parseInt(args[2]);
                username = args[3];
                password = args[4];
                break;
            case "withdraw":
            case "deposit":
                amount = Double.parseDouble(args[1]);
                break;
            case "inquiry":
                break;
            case "statement":
                startDate = new Date(args[1]);
                endDate = new Date(args[2]);
                break;
        }
    }
}
