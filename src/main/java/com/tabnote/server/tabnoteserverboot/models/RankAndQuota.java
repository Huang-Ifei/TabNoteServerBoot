package com.tabnote.server.tabnoteserverboot.models;

public class RankAndQuota {
    private int rank;
    private int quota;

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public int getQuota() {
        return quota;
    }

    public void setQuota(int quota) {
        this.quota = quota;
    }

    public boolean passAFABasic(){
        try {
            if (rank>0&&quota>0){
                return true;
            }
            return false;
        }catch (Exception e){
            return false;
        }
    }

    public boolean passAFAPlus(){
        try {
            if (rank>3&&quota>0){
                return true;
            }
            return false;
        }catch (Exception e){
            return false;
        }
    }

    public boolean passAFAPP(){
        try {
            if (rank>5&&quota>0){
                return true;
            }
            return false;
        }catch (Exception e){
            return false;
        }
    }
}
