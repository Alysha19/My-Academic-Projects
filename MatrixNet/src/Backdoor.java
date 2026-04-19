public class Backdoor {
    private final Host host1;
    private final Host host2;
    private final int latency;
    private final int bandwidth;
    private final int firewallLevel;
    private boolean sealed;

    public Backdoor(Host h1, Host h2, int latency, int bandwidth, int firewallLevel) {
        this.host1 = h1;
        this.host2 = h2;
        this.latency = latency;
        this.bandwidth = bandwidth;
        this.firewallLevel = firewallLevel;
        this.sealed = false;
    }

    public Host getOther(Host h) {
        return (h == host1) ? host2 : host1;
    }

    public Host getOtherByNumericId(int numId) {
        return (host1.numericId == numId) ? host2 : host1;
    }

    public void toggleSeal() {
        this.sealed = !this.sealed;
    }

    public Host getHost1() {
        return host1;
    }

    public Host getHost2() {
        return host2;
    }

    public int getLatency() {
        return latency;
    }

    public int getBandwidth() {
        return bandwidth;
    }

    public int getFirewallLevel() {
        return firewallLevel;
    }

    public boolean isSealed() {
        return sealed;
    }
}
