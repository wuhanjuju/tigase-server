/*
 * Tigase Jabber/XMPP Server
 * Copyright (C) 2004-2015 "Tigase, Inc." <office@tigase.com>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, version 3 of the License,
 * or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. Look for COPYING file in the top folder.
 * If not, see http://www.gnu.org/licenses/.
 */
package tigase.xmpp.impl;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import tigase.db.DBInitException;
import tigase.db.MsgRepositoryIfc;
import tigase.db.NonAuthUserRepository;
import tigase.db.UserNotFoundException;
import tigase.server.Packet;
import tigase.xml.Element;
import tigase.xmpp.BareJID;
import tigase.xmpp.JID;
import tigase.xmpp.XMPPResourceConnection;

/**
 *
 * @author andrzej
 */
public class OfflineMessagesTest extends ProcessorTestCase {
	
	private OfflineMessages offlineProcessor;
	private MsgRepositoryIfcImpl msgRepo;
	
	@Before
	@Override
	public void setUp() throws Exception {
		msgRepo = new MsgRepositoryIfcImpl();
		offlineProcessor = new OfflineMessages() {
			@Override
			protected MsgRepositoryIfc getMsgRepoImpl(NonAuthUserRepository repo, XMPPResourceConnection conn) {
				return msgRepo;
			}
		};
		offlineProcessor.init(new HashMap<String,Object>());
		super.setUp();
	}
	
	@After
	@Override
	public void tearDown() throws Exception {
		offlineProcessor = null;
		msgRepo = null;
		super.tearDown();
	}	

	@Test
	public void testStorageOfflineMessageForBareJid() throws Exception {
		BareJID userJid = BareJID.bareJIDInstance("user1@example.com");
		JID res1 = JID.jidInstance(userJid, "res1");
		JID res2 = JID.jidInstance(userJid, "res2");
		XMPPResourceConnection session1 = getSession(JID.jidInstance("c2s@example.com/" + UUID.randomUUID().toString()), res1);
		XMPPResourceConnection session2 = getSession(JID.jidInstance("c2s@example.com/" + UUID.randomUUID().toString()), res2);
		
		assertEquals(Arrays.asList(session1, session2), session1.getActiveSessions());
		
		Element packetEl = new Element("message", new String[] { "type", "from", "to" },
				new String[] { "chat", "remote-user@test.com/res1", userJid.toString() });
		packetEl.addChild(new Element("body", "Test message"));
		Packet packet = Packet.packetInstance(packetEl);
		Queue<Packet> results = new ArrayDeque<Packet>();
		offlineProcessor.postProcess(packet, session1, null, results, null);
		assertTrue("generated result even than no result should be generated", results.isEmpty());
		assertTrue("no message stored, while it should be stored", !msgRepo.getStored().isEmpty());
		
		msgRepo.getStored().clear();
		
		session1.setPriority(1);
		results = new ArrayDeque<Packet>();
		offlineProcessor.postProcess(packet, session1, null, results, null);
		assertTrue("generated result even than no result should be generated", results.isEmpty());
		assertTrue("no message stored, while it should be stored", !msgRepo.getStored().isEmpty());
		
		msgRepo.getStored().clear();	
		
		session1.setPresence(new Element("presence"));
		results = new ArrayDeque<Packet>();
		offlineProcessor.postProcess(packet, session1, null, results, null);
		assertTrue("generated result even than no result should be generated", results.isEmpty());
		assertTrue("message stored, while it should not be stored", msgRepo.getStored().isEmpty());		
		
		msgRepo.getStored().clear();
	}	
	
	@Test
	public void testStorageOfflineMessageForFullJid() throws Exception {
		BareJID userJid = BareJID.bareJIDInstance("user1@example.com");
		JID res1 = JID.jidInstance(userJid, "res1");
		JID res2 = JID.jidInstance(userJid, "res2");
		XMPPResourceConnection session1 = getSession(JID.jidInstance("c2s@example.com/" + UUID.randomUUID().toString()), res1);
		XMPPResourceConnection session2 = getSession(JID.jidInstance("c2s@example.com/" + UUID.randomUUID().toString()), res2);
		
		assertEquals(Arrays.asList(session1, session2), session1.getActiveSessions());
		
		Element packetEl = new Element("message", new String[] { "type", "from", "to" },
				new String[] { "chat", "remote-user@test.com/res1", res2.toString() });
		packetEl.addChild(new Element("body", "Test message"));
		Packet packet = Packet.packetInstance(packetEl);
		Queue<Packet> results = new ArrayDeque<Packet>();
		offlineProcessor.postProcess(packet, session1, null, results, null);
		assertTrue("generated result even than no result should be generated", results.isEmpty());
		assertTrue("no message stored, while it should be stored", !msgRepo.getStored().isEmpty());
		
		msgRepo.getStored().clear();
		
		session1.setPriority(1);
		results = new ArrayDeque<Packet>();
		offlineProcessor.postProcess(packet, session1, null, results, null);
		assertTrue("generated result even than no result should be generated", results.isEmpty());
		assertTrue("no message stored, while it should be stored", !msgRepo.getStored().isEmpty());
		
		msgRepo.getStored().clear();	
		
		session1.setPresence(new Element("presence"));
		results = new ArrayDeque<Packet>();
		offlineProcessor.postProcess(packet, session1, null, results, null);
		assertTrue("generated result even than no result should be generated", results.isEmpty());
		assertTrue("message stored, while it should not be stored", msgRepo.getStored().isEmpty());		
		
		msgRepo.getStored().clear();
	}		

	@Test
	public void testLoadOfflineMessages() throws Exception {
		BareJID userJid = BareJID.bareJIDInstance("user1@example.com");
		JID res1 = JID.jidInstance(userJid, "res1");
		JID res2 = JID.jidInstance(userJid, "res2");
		XMPPResourceConnection session1 = getSession(JID.jidInstance("c2s@example.com/" + UUID.randomUUID().toString()), res1);
		XMPPResourceConnection session2 = getSession(JID.jidInstance("c2s@example.com/" + UUID.randomUUID().toString()), res2);
		
		assertEquals(Arrays.asList(session1, session2), session1.getActiveSessions());
		
		Element presenceEl = new Element("presence", new String[] { "from", "to" }, new String[] { res1.toString(), res2.toString() });
		Packet packet = Packet.packetInstance(presenceEl);
		assertFalse(offlineProcessor.loadOfflineMessages(packet, session1));
		
		presenceEl = new Element("presence", new String[] { "from" }, new String[] { res1.toString() });
		packet = Packet.packetInstance(presenceEl);
		assertTrue(offlineProcessor.loadOfflineMessages(packet, session1));	
	}	
	
	private static class MsgRepositoryIfcImpl implements MsgRepositoryIfc {

		private final Queue<Packet> stored = new ArrayDeque();
		
		public MsgRepositoryIfcImpl() {
		}

		@Override
		public Element getMessageExpired(long time, boolean delete) {
			throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
		}

		@Override
		public Queue<Element> loadMessagesToJID(JID to, boolean delete) throws UserNotFoundException {
			throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
		}
		
		@Override
		public void storeMessage(JID from, JID to, Date expired, Element msg) throws UserNotFoundException {
			stored.offer(Packet.packetInstance(msg, from, to));
		}
		
		@Override
		public void initRepository(String resource_uri, Map<String, String> params) throws DBInitException {
		}
		
		public Queue<Packet> getStored() {
			return stored;
		}
	}
	
}
