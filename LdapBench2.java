//
// A lame first attempt at importing data into an LDAP server
//

import java.util.*;

import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPAttributeSet;
import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPSearchResults;
import com.novell.ldap.LDAPMessageQueue;
import com.novell.ldap.LDAPResponseQueue;
import com.novell.ldap.LDAPSearchConstraints;
import com.novell.ldap.util.Base64;

// This is so I can do UTF8 encoded strings which the ldap bind call demands
// or you get a "deprecated" warning at compile time
import java.io.UnsupportedEncodingException;

public class LdapBench2 {
   // Test Types
   public static final int INSERT_TEST=0;
   public static final int DELETE_TEST=1;
   public static final int UPDATE_TEST=2;
   public static final int SEARCH_TEST=3;
    
   private static LDAPConnection doBind (
      String ldapHost,
      String loginDN,
      String password
   ) {
       int ldapPort = LDAPConnection.DEFAULT_PORT;
       int ldapVersion  = LDAPConnection.LDAP_V3;
       //
       // connect to the LDAP server
       LDAPConnection lc = new LDAPConnection();

       try {
         // connect to the server
         lc.connect( ldapHost, ldapPort );

         // bind to the server
         try {
            lc.bind( ldapVersion, loginDN, password.getBytes("UTF8") );
         }
         catch( LDAPException e ) {
            System.out.println( "doBind: Error: " + e.toString() );
            System.out.println( "Trying to close connection and exit\n" );
            lc.disconnect();
            return (null);
         }
      }
      catch( LDAPException e ) {
         System.out.println( "doBind: Error: " + e.toString() );
         return (null);
      }
      catch( UnsupportedEncodingException e ) {
          System.out.println( "doBind: Error: " + e.toString() );
         return (null);
      }

      return (lc);
   }


   // Close the LDAP server connection
   private static void doClose ( LDAPConnection lc ) {
      try { lc.disconnect(); }
      catch( LDAPException e ) {
          System.out.println( "doCloseError: " + e.toString() );
      }
   }

   // Create LDAP records
   private static void doCreate (
      LDAPConnection lc,
      String ouName,
      String baseDN,
      int numRecs
   ) {
      LDAPAttributeSet attrs = new LDAPAttributeSet();
      attrs.add ( new LDAPAttribute("objectclass","organizationalunit") );
      attrs.add ( new LDAPAttribute("ou",ouName) );
      String dn = "ou=" + ouName + "," + baseDN;

      // Now create an LDAPEntry and add it
      LDAPEntry entry = new LDAPEntry(dn,attrs);
      try { lc.add (entry); }
      catch( LDAPException e ) {
         System.out.println( "doClose: Error: " + e.toString() );
      }

      // Add some records here beneath the ou we just created
      String myBase = dn;

      for (int i=0 ; i < numRecs; i++ ) {
         // First, create a set of attributes
         attrs = new LDAPAttributeSet();
         attrs.add ( new LDAPAttribute("objectclass","person") );
         attrs.add ( new LDAPAttribute("cn",("Bobo Gorilla"+i)) );
         attrs.add ( new LDAPAttribute("sn",("Gorilla"+i)) );
         dn = "cn=Bobo Gorilla" + i + "," + myBase;
 
         // Now create an LDAPEntry
         entry = new LDAPEntry(dn,attrs);
 
         // And finally, try to Add the entry
         //System.out.println ("Adding DN: " + dn + "\n");
         try { lc.add (entry); }
         catch( LDAPException e ) {
            System.out.println( "doClose: Error: " + e.toString() );
         }
      }
   }

   // Find LDAP records
   private static void doSearch (
      LDAPConnection lc,
      String ouName,
      String baseDN,
      int numRecs
   ) {
      String myBase = "ou=" + ouName + "," + baseDN;
      String filter;
      String attrs[] = {"cn"};
      LDAPSearchResults lsr;

      for (int i=0 ; i < numRecs; i++ ) {
         filter = "(cn=Bobo Gorilla" + i + ")";
 
         // And finally, try to Add the entry
         try {
            lsr = lc.search (myBase,lc.SCOPE_SUB,filter,attrs,false);
	    while (lsr.hasMore()) {
               LDAPEntry lde = lsr.next();
	       //System.out.println (ouName + ": found " + lde.getDN());
	       lde = null;
            }
         }
         catch( LDAPException e ) {
            System.out.println( "doSearch: Error: " + e.toString() );
         }
      }
   }

   // Delete LDAP records
   private static void doDelete (
      LDAPConnection lc,
      String ouName,
      String baseDN,
      int numRecs
   ) {
      String myBase = "ou=" + ouName + "," + baseDN;
      String dn;

      // First, delete all the child records of myBase
      for (int i=0 ; i < numRecs; i++ ) {
         dn = "cn=Bobo Gorilla" + i + "," + myBase;
         try { lc.delete (dn); }
         catch( LDAPException e ) {
            System.out.println( "doDelete: Error: " + e.toString() );
         }
      }

      // Finally, delete this thread's baseDN as well
      try { lc.delete (myBase); }
      catch( LDAPException e ) {
         System.out.println( "doDelete: Error: " + e.toString() );
      }
   }

   public static void runTest (
      int testType, 
      String ldapURI,
      String bindDN,
      String ldapPass,
      String baseDN,
      int numRecs,
      int numThreads
   ) {
      String units = "";

      // Make worker threads
      Vector<Thread> threadList = new Vector<Thread>(numThreads);
      final String threadBase = baseDN;
      final int threadNumRecs = numRecs;

      for (int i = 0; i < numThreads; i++) {
         final String threadOu = "Thread " + i;
         final LDAPConnection threadLc = doBind (ldapURI, bindDN, ldapPass);

         switch (testType) {
         case INSERT_TEST:
            units = "Adds/second";
	    threadList.add( new Thread() {
                  public void run () {
                     doCreate (threadLc, threadOu, threadBase, threadNumRecs);
                     doClose (threadLc);
                  }
               });
	    break;
         case DELETE_TEST:
            units = "Deletes/second";
	    threadList.add( new Thread() {
                  public void run () {
                     doDelete (threadLc, threadOu, threadBase, threadNumRecs);
                     doClose (threadLc);
                  }
               });
	    break;
         case SEARCH_TEST:
            units = "Queries/second";
	    threadList.add( new Thread() {
                  public void run () {
                     doSearch (threadLc, threadOu, threadBase, threadNumRecs);
                     doClose (threadLc);
                  }
               });
	    break;
         }
      }

      // Get start time
      long startTime = new java.util.Date().getTime();

      // Start the threads running, then wait for them all to finish
      try {
         for (int i=0; i < numThreads; i++) {
            ((Thread)threadList.get(i)).start();
         }
         for (int i=0; i < numThreads; i++) {
            ((Thread)threadList.get(i)).join();
         }
      }
      catch( InterruptedException e ) {
         System.out.println( "runTest: Error: " + e.toString() );
      }

      // Do the Math - divide totalTime by 1000 since it's in ms
      float totalTime = (float)(new java.util.Date().getTime() - startTime);
      float throughput = (float)(numThreads * numRecs) / (totalTime / 1000);
      System.out.println("Total time: " + totalTime + " ms, Throughput: " +
		      throughput + " " + units);
      System.out.println("--------------------------------------------");
   }

   public static void main( String[] args ) {
      if (args.length != 6) {
         System.out.println("This program imports stuff into an LDAP tree\n");
         System.out.println("Usage:   java LdapBench <host name> <login dn>"
                            + " <password> <baseDN> <numThreads>"
			    + " <numToCreate>\n");
         System.exit(0);
      }

      String ldapURI = args[0];
      String bindDN = args[1];
      String ldapPass = args[2];
      String baseDN = args[3];
      int numThreads = Integer.parseInt(args[4]);
      int numRecs = Integer.parseInt(args[5]);

//      LDAPConnection lc = doBind (ldapURI, bindDN, ldapPass);
//
//      if (lc == null) {
//         System.exit(0);
//      }

      runTest (INSERT_TEST,ldapURI,bindDN,ldapPass,baseDN,numRecs,numThreads);
      runTest (SEARCH_TEST,ldapURI,bindDN,ldapPass,baseDN,numRecs,numThreads);
      runTest (DELETE_TEST,ldapURI,bindDN,ldapPass,baseDN,numRecs,numThreads);

      //doClose (lc);
      System.exit(0);
   }

}
