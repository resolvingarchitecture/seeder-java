package ra.seeder;

import ra.common.Status;
import ra.i2p.I2PService;
import ra.servicebus.ServiceBus;
import ra.util.Config;
import ra.util.SecureFile;
import ra.util.SystemSettings;
import ra.util.Wait;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Properties;
import java.util.Random;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Daemon {

    private static final Logger LOG = Logger.getLogger(Daemon.class.getName());

    private static final Daemon instance = new Daemon();

    private File baseDir;
    private File configDir;
    private File libDir;
    private File pidDir;
    private File logDir;
    private File dataDir;
    private File cacheDir;
    private volatile File tmpDir;
    private final Random tmpDirRand = new Random();
    private final static Object lockA = new Object();
    private boolean initialize = false;
    private boolean configured = false;
    private Locale locale;
    private String version = null;
    // split up big lock on this to avoid deadlocks
    private final Object lock1 = new Object();

    private ServiceBus bus;
    private Properties config;
    private Status status = Status.Stopped;

    public static void main(String[] args) {
        instance.start(args);
    }

    public void start(String[] args) {
        LOG.info("RA Seeder initializing...");
        Thread.currentThread().setName("RA-Seeder-Thread");
        LOG.info("Thread name: " + Thread.currentThread().getName());

        status = Status.Starting;

        try {
            config = Config.loadFromMainArgsAndClasspath(args, "ra-seeder.config",false);
        } catch (Exception e) {
            LOG.severe(e.getLocalizedMessage());
            System.exit(-1);
        }

        String logPropsPathStr = config.getProperty("java.util.logging.config.file");
        if(logPropsPathStr != null) {
            File logPropsPathFile = new File(logPropsPathStr);
            if(logPropsPathFile.exists()) {
                try {
                    FileInputStream logPropsPath = new FileInputStream(logPropsPathFile);
                    LogManager.getLogManager().readConfiguration(logPropsPath);
                } catch (IOException e1) {
                    LOG.warning(e1.getLocalizedMessage());
                }
            }
        }

        version = config.getProperty("ra.seeder.version")+"."+config.getProperty("ra.seeder.version.build");
        LOG.info("RA Seeder Version: "+version);
        System.setProperty("ra.seeder.version", version);

        String baseStr = null;
        try {
            baseDir = SystemSettings.getUserAppHomeDir(".ra","seeder",true);
        } catch (IOException e) {
            LOG.warning(e.getLocalizedMessage());
            return;
        }
        if(baseDir!=null) {
            config.put("ra.seeder.dir.base", baseDir.getAbsolutePath());
        } else {
            baseDir = SystemSettings.getSystemApplicationDir(".ra", "seeder", true);
            if (baseDir == null) {
                LOG.severe("Unable to create base system directory for Seeder app.");
                return;
            } else {
                baseStr = baseDir.getAbsolutePath();
                config.put("ra.seeder.dir.base", baseStr);
            }
        }

        configDir = new SecureFile(baseDir, "config");
        if(!configDir.exists() && !configDir.mkdir()) {
            LOG.severe("Unable to create config directory in Seeder base directory.");
            return;
        } else {
            config.put("ra.seeder.dir.config",configDir.getAbsolutePath());
        }

        libDir = new SecureFile(baseDir, "lib");
        if(!libDir.exists() && !libDir.mkdir()) {
            LOG.severe("Unable to create lib directory in Seeder base directory.");
            return;
        } else {
            config.put("ra.seeder.dir.lib",libDir.getAbsolutePath());
        }

        dataDir = new SecureFile(baseDir, "data");
        if(!dataDir.exists() && !dataDir.mkdir()) {
            LOG.severe("Unable to create data directory in Seeder base directory.");
            return;
        } else {
            config.put("ra.seeder.dir.data",dataDir.getAbsolutePath());
        }

        cacheDir = new SecureFile(baseDir, "cache");
        if(!cacheDir.exists() && !cacheDir.mkdir()) {
            LOG.severe("Unable to create cache directory in Seeder base directory.");
            return;
        } else {
            config.put("ra.seeder.dir.cache",cacheDir.getAbsolutePath());
        }

        pidDir = new SecureFile(baseDir, "pid");
        if (!pidDir.exists() && !pidDir.mkdir()) {
            LOG.severe("Unable to create pid directory in Seeder base directory.");
            return;
        } else {
            config.put("ra.seeder.dir.pid",pidDir.getAbsolutePath());
        }

        logDir = new SecureFile(baseDir, "logs");
        if (!logDir.exists() && !logDir.mkdir()) {
            LOG.severe("Unable to create logs directory in Seeder base directory.");
            return;
        } else {
            config.put("ra.seeder.dir.log",logDir.getAbsolutePath());
        }

        tmpDir = new SecureFile(baseDir, "tmp");
        if (!tmpDir.exists() && !tmpDir.mkdir()) {
            LOG.severe("Unable to create tmp directory in Seeder base directory.");
            return;
        } else {
            config.put("ra.seeder.dir.temp",tmpDir.getAbsolutePath());
        }

        LOG.info("Seeder Directories: " +
                "\n\tBase: "+baseDir.getAbsolutePath()+
                "\n\tConfig: "+configDir.getAbsolutePath()+
                "\n\tData: "+dataDir.getAbsolutePath()+
                "\n\tCache: "+cacheDir.getAbsolutePath()+
                "\n\tPID: "+pidDir.getAbsolutePath()+
                "\n\tLogs: "+logDir.getAbsolutePath()+
                "\n\tTemp: "+tmpDir.getAbsolutePath());

        bus = new ServiceBus();
        bus.start(config);

        try {
            bus.registerService(I2PService.class, config, null);
        } catch (Exception e) {
            LOG.severe(e.getLocalizedMessage());
            System.exit(-1);
        }
        status = Status.Running;

        // Check periodically to see if seeder stopped
        while (status == Status.Running) {
            Wait.aSec(2);
        }

        System.exit(0);
    }

    public void shutdown() {
        status = Status.Stopping;
        LOG.info("Seeder Shutting Down...");
        status = Status.Stopped;
    }

}
