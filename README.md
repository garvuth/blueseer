<img src="https://github.com/blueseerERP/blueseer/blob/master/src/images/bs65image.png" alt="Free ERP">
<!-- <img class="logo" width="100px" height="100px" src="https://www.blueseer.com/img/bs.png" alt="Free ERP"> -->
<a href="https://foojay.io/today/works-with-openjdk"><img align="right" src="https://github.com/foojayio/badges/raw/main/works_with_openjdk/Works-with-OpenJDK.png" width="100"></a>
<h3>Developer: Terry Vaughn</h3>
<h3>latest release version: 6.7</h3>
<h3>latest release date: 2023-09-22</h3>
<h3>programming language: Java programming language</h3> 
<h3>operating system: Cross-Platform</h3>
<h3>genre:  Enterprise Resource Planning (ERP), EDI, Accounting, Personal Finance</h3> 
<h3>languages supported: English, French, Spanish, Turkish, German, Romanian</h3>
<h3>license: MIT License</h3>
<h3>website: www.blueseer.com</h3>



<img src="https://github.com/blueseerERP/blueseer/blob/master/src/images/market2.png" alt="Free ERP image 2">
'''BlueSeer ERP''' is a Free open source multilingual ERP software package.  It was designed to meet the needs of
the manufacturing community for an ERP system that is easily customizable and
extendable while providing generic functionality that is typically observed in
most manufacturing environments.  BlueSeer also provides a fully functional EDI mapping tool for EDI translations and file traffic monitoring. 
BlueSeer is released for free use under the MIT License.   The application and source code
are available for download at github.com. BlueSeer was originally launched in 2017 and continues to evolve to meet user demands.
The latest 'stable' release of version 6.7 was released on 2023-09-22.</br>

<h1>Functionality</h1>

BlueSeer provides modules for the following generic set of business concepts : 
* Double Entry General Ledger
* Cost Accounting
* Accounts Receivable Processing and Aging
* Accounts Payable Processing and Aging
* PayRoll
* APIs for system to system integration
* Inventory Control
* Job Tracking
* Lot Traceability
* Order Management
* Service Order and Quoting Management
* Freight Management
* Electronic Data Interchange (EDI)
* EDI Mapping tool (supports: X12, EDIFACT, CSV, FlatFile [IDOC, etc], XML, JSON )
* EDI Communications (FTP, AS2 server/client)
* Automated Task/Cron Scheduler
* UCC Label Generation
* Materials Resource Planning (MRP)
* Human Resources (HR)

<h2>Technology</h2>
BlueSeer ERP is written entirely in Java.  The application is a non-web based
desktop application that relies heavily on the Java Swing widget
toolkit/library.  There are currently two database engines available for
BlueSeer. 
For single client deployment, The relational database SQLite is used for
it's deployment ease and server-less design.  For multi-client
deployment scenarios, the open-source relational database MySQL is used as the
back-end database server.  MySQL was chosen for it's popularity and excellent
performance
reviews.  
</br>
BlueSeer is a menu-driven application.  It's composition is a collection of Java Swing
JPanel widgets.  Each business function, i.e. Order Entry, Item Master
Maintenance, etc is a stand-alone JPanel widget.  Each JPanel widget is loaded
at runtime using Reflection to 'inject' the JPanel
into the JFrame on user
demand.  JPanel class names are stored in the database and associated
with  menu options which are further associated with user permissions.  This
archtitecture increases the capability of customization and extension by
engaging BlueSeer as
a Desktop Application Framework.  Applications independent of the core
software can be quickly deployed 
given the menu/class management and
permissions functionality that's built into the BlueSeer framework.
</br>


