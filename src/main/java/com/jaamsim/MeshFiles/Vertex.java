/*
 * JaamSim Discrete Event Simulation
 * Copyright (C) 2013 Ausenco Engineering Canada Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
package com.jaamsim.MeshFiles;

import com.jaamsim.math.Vec4d;

/**
 * Vertex is a simple data container class, mostly needed for its equals() and hashcode() overrides
 * this allows us to build up semi-efficient hash maps of vertex data when building geometry
 * @author matt.chudleigh
 *
 */
public class Vertex {

	private Vec4d position;
	private Vec4d normal;
	private Vec4d texCoord;

	private int cachedHash = 0;

	private static int hashVec4d(Vec4d v) {
		int hash = 0;
		hash = hash ^ Double.valueOf(v.x).hashCode();
		hash = hash ^ Double.valueOf(v.y).hashCode();
		hash = hash ^ Double.valueOf(v.z).hashCode();
		hash = hash ^ Double.valueOf(v.w).hashCode();
		return hash;
	}

	public Vertex(Vec4d position, Vec4d normal, Vec4d texCoord) {
		this.position = position;
		this.normal = normal;
		this.texCoord = texCoord;

		cachedHash = cachedHash ^ hashVec4d(position);
		cachedHash = cachedHash ^ hashVec4d(normal);
		if (texCoord != null) {
			cachedHash = cachedHash ^ hashVec4d(texCoord);
		}
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Vertex)) {
			return false;
		}
		Vertex ov = (Vertex)o;
		if (position != ov.position && !position.equals4(ov.position)) {
			return false;
		}
		if (normal != ov.normal && !normal.equals4(ov.normal)) {
			return false;
		}

		// One has a texCoord, but not the other
		if ((texCoord == null && ov.texCoord != null) ||
		    (texCoord != null && ov.texCoord == null)) {
			return false;
		}

		if (texCoord != null && texCoord != ov.texCoord && !texCoord.equals4(ov.texCoord)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		return cachedHash;
	}

	public Vec4d getPos() {
		return position;
	}

	public Vec4d getNormal() {
		return normal;
	}

	public Vec4d getTexCoord() {
		return texCoord;
	}
}