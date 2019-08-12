package io.github.thatkawaiisam.ziggurat;

public class ZigguratThread extends Thread {

    private TablistManager tablistManager;

    public ZigguratThread(TablistManager tablistManager) {
        this.tablistManager = tablistManager;

        this.start();
    }

    @Override
    public void run() {
        while (true) {
            try {
                tick();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            try {
                sleep(tablistManager.getTicks() * 50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void tick() {
        tablistManager.getTablists().values().forEach(PlayerTablist::update);
    }
}
