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
import java.util.Timer;
import java.util.TimerTask;

//Client program, which connects to the bank using RMI and class methods of the remote bank object
public class ATM {
    private static String serverAddress1, serverAddress2;
    private static int serverPort1, serverPort2, account1, account2;
    private static String name1, name2;
    private static String operation, username, password;
    private static long sessionID1 = 0;
    private static long sessionID2 = 0;
    private static double amount;
    private static BankInterface bank1, bank2;
    private static Date startDate, endDate;
    private volatile static long timeoutPeriod;
    private volatile static int leaseTime;
    private static Account acc1, acc2;
    private static Timer heartbeatTimer1;
    private static Timer heartbeatTimer2;
    private static Timer loginTimer;
    private static int hbCount1;
    private static boolean bank1Alive;
    private static boolean bank2Alive;

    private static class LoginTask extends TimerTask {

        @Override
        public void run() {
            //System.out.println("login again!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            if (!bank1Alive) {
                //System.out.println("login bank1!!!!!!!!!!!!!");
                login1();
            }

            if (!bank2Alive) {
                //System.out.println("login bank2!!!!!!!!!!!!!!!!!!!!");
                login2();
            }
        }
    }

    private static class HeartbeatTask extends TimerTask {
        private BankInterface bank;
        private Timer heartbeatTimer;
        private String name;
        private int count = 0;

        HeartbeatTask(BankInterface bank, Timer heartbeatTimer, String name)
        {
            this.bank = bank;
            this.heartbeatTimer = heartbeatTimer;
            this.name = name;
        }

        public void run() {
            try{
                boolean isAlive = bank.heartbeat();
                //System.out.println("sent heartbeat meesage");
            }
            catch(Exception e){
                count += 1;
                System.out.println("count + 1");
                if (count >= leaseTime) {
                    System.out.println("Server missing! This is a test, may print a lot of time.");
                    this.heartbeatTimer.cancel();
                    this.heartbeatTimer.purge();
//                hbCount++;

                    // System.out.println("count is " + hbCount);

//                if (hbCount == 2) {
//                    System.exit(0);
//                }
                    if (name.equals(name1)) {
                        bank1Alive = false;
                    }
                    if (name.equals(name2)) {
                        bank2Alive = false;
                    }

                    if (!bank1Alive && !bank2Alive) {
                        System.exit(0);
                    }

                    //System.exit(0);
                }
            }
        }
    }

    private static void login1() {
        try {
            //Login with username and password
            //Set up the rmi registry and get the remote bank object from it
            Registry registry = LocateRegistry.getRegistry(serverAddress1, serverPort1);
            bank1 = (BankInterface) registry.lookup(name1);

            sessionID1 = bank1.login(username, password);
            acc1 = bank1.accountDetails(sessionID1);
            account1 = acc1.getAccountNumber();
            //Print account details
            System.out.println("\n----------------\nClient Connected bank 1" + "\n----------------\n");
            System.out.println("--------------------------\nAccount Details:\n--------------------------\n" +
                    "Account Number: " + acc1.getAccountNumber() +
                    "\nUsername: " + acc1.getUserName() +
                    "\nBalance: " + acc1.getBalance() +
                    "\n--------------------------\n");
            //System.out.println("Session active for 5 minutes");
            //System.out.println("Use SessionID " + sessionID1 + " for all other operations");
            // Heartbeat
            heartbeatTimer1 = new Timer();
            long freq = timeoutPeriod / leaseTime;
            heartbeatTimer1.scheduleAtFixedRate (new HeartbeatTask(bank1, heartbeatTimer1, name1), 0, timeoutPeriod);
            bank1Alive = true;
            //Catch exceptions that can be thrown from the server
        } catch (RemoteException e) {
            //System.out.println("bank 1 connect lose");
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

    }

    private static void login2() {
        try {
            //Login with username and password
            //Set up the rmi registry and get the remote bank object from it
            Registry registry = LocateRegistry.getRegistry(serverAddress2, serverPort2);
            bank2 = (BankInterface) registry.lookup(name2);

            sessionID2 = bank2.login(username, password);
            acc2 = bank2.accountDetails(sessionID2);
            account2 = acc2.getAccountNumber();
            //Print account details
            System.out.println("\n----------------\nClient Connected bank 2" + "\n----------------\n");
            System.out.println("--------------------------\nAccount Details:\n--------------------------\n" +
                    "Account Number: " + acc2.getAccountNumber() +
                    "\nUsername: " + acc2.getUserName() +
                    "\nBalance: " + acc2.getBalance() +
                    "\n--------------------------\n");
            //System.out.println("Session active for 5 minutes");
            //System.out.println("Use SessionID " + sessionID2 + " for all other operations");
            // Heartbeat
            heartbeatTimer2 = new Timer();
            long freq = timeoutPeriod / leaseTime;
            heartbeatTimer2.scheduleAtFixedRate (new HeartbeatTask(bank2, heartbeatTimer2, name2), 0, freq);
            bank2Alive = true;
            //Catch exceptions that can be thrown from the server
        } catch (RemoteException e) {
            //System.out.println("bank 2 connect lose");
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

    }

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
            //double balance;

            System.out.println("login method finish");

            //Switch based on the operation
            switch (operation){
                case "exit":
                    System.out.println("User exit ATM terminal.");
                    System.exit(0);

                case "login":
                    login1();
                    login2();
                    loginTimer = new Timer();
                    loginTimer.scheduleAtFixedRate (new LoginTask(), 0, 1000);
                    break;

                case "deposit":
                    boolean depositSuccess1 = false;

                    try {
                        //Make bank deposit and get updated balance
                        double balance = bank1.deposit(account1, amount, sessionID1);
                        System.out.println("Successfully deposited E" + amount + " into account " + account1);
                        System.out.println("New balance: E" + balance);
                        depositSuccess1 = true;
                        //Catch exceptions that can be thrown from the server
                    } catch (RemoteException e) {
                        System.out.println("bank 1 connect lose");
                        //e.printStackTrace();
                    } catch (InvalidSessionException e) {
                        System.out.println("Please login bank 1 again");
                        //System.out.println(e.getMessage());
                    }

                    try {
                        //Make bank deposit and get updated balance
                        double balance = bank2.deposit(account2, amount, sessionID2);
                        if (!depositSuccess1) {
                            System.out.println("Successfully deposited E" + amount + " into account " + account2);
                            System.out.println("New balance: E" + balance);
                        }
                        //Catch exceptions that can be thrown from the server
                    } catch (RemoteException e) {
                        System.out.println("bank 2 connect lose");
                        //e.printStackTrace();
                    } catch (InvalidSessionException e) {
                        System.out.println("Please login bank 2 again");
                        //System.out.println(e.getMessage());
                    }

                    break;

                case "withdraw":
                    boolean withdrawSuccess1 = false;
                    try {
                        //Make bank withdrawal and get updated balance
                        double balance = bank1.withdraw(account1, amount, sessionID1);
                        System.out.println("Successfully withdrew E" + amount + " from account " + account1 +
                                "\nRemaining Balance: E" + balance);
                        withdrawSuccess1 = true;
                        //Catch exceptions that can be thrown from the server
                    } catch (RemoteException e) {
                        System.out.println("bank 1 connect lose");
                        //e.printStackTrace();
                    } catch (InvalidSessionException e) {
                        System.out.println("Please login bank 1 again");
                        //System.out.println(e.getMessage());
                    } catch (InsufficientFundsException e) {
                        System.out.println("Not enough money");
                        //System.out.println(e.getMessage());
                    }

                    try {
                        //Make bank withdrawal and get updated balance
                        double balance = bank2.withdraw(account2, amount, sessionID2);
                        if (!withdrawSuccess1) {
                            System.out.println("Successfully withdrew E" + amount + " from account " + account2 +
                                    "\nRemaining Balance: E" + balance);
                        }
                        //Catch exceptions that can be thrown from the server
                    } catch (RemoteException e) {
                        System.out.println("bank 2 connect lose");
                        //e.printStackTrace();
                    } catch (InvalidSessionException e) {
                        System.out.println("Please login bank 2 again");
                        //System.out.println(e.getMessage());
                    } catch (InsufficientFundsException e) {
                        System.out.println("Not enough money");
                        //System.out.println(e.getMessage());
                    }

                    break;

                case "inquiry":
                    boolean inquirySuccess1 = false;
                    try {
                        //Get account details from bank
                        Account acc1 = bank1.inquiry(account1,sessionID1);
                        System.out.println("--------------------------\nAccount Details:\n--------------------------\n" +
                                "Account Number: " + acc1.getAccountNumber() +
                                "\nUsername: " + acc1.getUserName() +
                                "\nBalance: E" + acc1.getBalance() +
                                "\n--------------------------\n");
                        inquirySuccess1 = true;
                        //Catch exceptions that can be thrown from the server
                    } catch (RemoteException e) {
                        System.out.println("bank 1 connect lose");
                        //e.printStackTrace();
                    } catch (InvalidSessionException e) {
                        System.out.println("Please login again");
                        //System.out.println(e.getMessage());
                    }

                    try {
                        //Get account details from bank
                        Account acc2 = bank2.inquiry(account2,sessionID2);
                        if (!inquirySuccess1) {
                            System.out.println("--------------------------\nAccount Details:\n--------------------------\n" +
                                    "Account Number: " + acc2.getAccountNumber() +
                                    "\nUsername: " + acc2.getUserName() +
                                    "\nBalance: E" + acc2.getBalance() +
                                    "\n--------------------------\n");
                        }
                        //Catch exceptions that can be thrown from the server
                    } catch (RemoteException e) {
                        System.out.println("bank 2 connect lose");
                        //e.printStackTrace();
                    } catch (InvalidSessionException e) {
                        System.out.println("Please login  bank2  again");
                        //System.out.println(e.getMessage());
                    }
                    break;

                case "statement":
                    Statement s = null;
                    try {
                        //Get statement for required dates
                        s = (Statement) bank1.getStatement(account1, startDate, endDate, sessionID1);

                        //format statement for printing to the window
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                        System.out.print("-----------------------------------------------------------------------\n");
                        System.out.println("Statement for Account " + account1 + " between " +
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
                        System.out.println("bank 1 connect lose");
                        //e.printStackTrace();
                    } catch (InvalidSessionException e) {
                        System.out.println("Please login again");
                        //System.out.println(e.getMessage());
                    } catch (StatementException e) {
                        System.out.println(e.getMessage());
                    }

                    if (s == null) {
                        try {
                            //Get statement for required dates
                            s = (Statement) bank2.getStatement(account2, startDate, endDate, sessionID2);

                            //format statement for printing to the window
                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                            System.out.print("-----------------------------------------------------------------------\n");
                            System.out.println("Statement for Account " + account2 + " between " +
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
            case "setheartbeat":
                timeoutPeriod = Long.parseLong(args[1]);
                leaseTime = Integer.parseInt(args[2]);
                break;
            case "exit":
                return;
            case "login":
                serverAddress1 = args[1];
                serverPort1 = Integer.parseInt(args[2]);
                name1 = args[3];
                serverAddress2 = args[4];
                serverPort2 = Integer.parseInt(args[5]);
                name2 = args[6];
                username = args[7];
                password = args[8];
                timeoutPeriod = Long.parseLong(args[9]);
                leaseTime = Integer.parseInt(args[10]);
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
