/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package model;

/**
 *
 * @author dimas
 */
public interface Approvable {
    public boolean approve(Admin admin);
    public boolean reject(Admin admin);
    public String getStatus();
}
