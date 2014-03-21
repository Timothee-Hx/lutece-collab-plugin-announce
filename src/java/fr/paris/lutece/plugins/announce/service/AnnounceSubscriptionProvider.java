/*
 * Copyright (c) 2002-2014, Mairie de Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.announce.service;

import fr.paris.lutece.plugins.announce.business.AnnounceSearchFilter;
import fr.paris.lutece.plugins.announce.business.AnnounceSearchFilterHome;
import fr.paris.lutece.plugins.announce.business.CategoryHome;
import fr.paris.lutece.plugins.announce.web.AnnounceApp;
import fr.paris.lutece.plugins.subscribe.business.Subscription;
import fr.paris.lutece.plugins.subscribe.business.SubscriptionFilter;
import fr.paris.lutece.plugins.subscribe.service.ISubscriptionProviderService;
import fr.paris.lutece.plugins.subscribe.service.SubscriptionService;
import fr.paris.lutece.portal.service.security.LuteceUser;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.web.LocalVariables;
import fr.paris.lutece.util.html.HtmlTemplate;

import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


/**
 * Subscription provider service for announces
 */
public class AnnounceSubscriptionProvider implements ISubscriptionProviderService
{
    /**
     * Subscription key to subscribe to announces published by users
     */
    public static final String SUBSCRIPTION_USER = "announce_user";
    /**
     * Subscription key to subscribe to announces of a given category
     */
    public static final String SUBSCRIPTION_CATEGORY = "announce_category";
    /**
     * Subscription key to subscribe to announces matching a filter
     */
    public static final String SUBSCRIPTION_FILTER = "announce_filter";

    // Markers
    private static final String MARK_FILTER = "filter";
    private static final String MARK_CATEGORY = "category";

    private static final String PROVIDER_NAME = "announce.announceSubscriptionProvider";
    private static final String BEAN_NAME = "announce.announceSubscriptionProvider";

    // Templates
    private static final String TEMPLATE_FILTER_SUBSCRIPTION_DESCRIPTION = "skin/plugins/announce/filter_subscriction_description.html";

    private static AnnounceSubscriptionProvider _instance;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getProviderName( )
    {
        return PROVIDER_NAME;
    }

    /**
     * Get the instance of the service
     * @return The instance of the service
     */
    public static AnnounceSubscriptionProvider getService( )
    {
        if ( _instance == null )
        {
            synchronized ( AnnounceSubscriptionProvider.class )
            {
                _instance = SpringContextService.getBean( BEAN_NAME );
            }
        }
        return _instance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSubscriptionHtmlDescription( LuteceUser user, String strSubscriptionKey,
            String strIdSubscribedResource, Locale locale )
    {
        int nId = ( ( strIdSubscribedResource != null ) && StringUtils.isNumeric( strIdSubscribedResource ) ) ? Integer
                .parseInt( strIdSubscribedResource ) : 0;

        if ( nId > 0 )
        {
            if ( StringUtils.equals( SUBSCRIPTION_USER, strSubscriptionKey ) )
            {
                return StringUtils.EMPTY; //TODO : implement me !
            }
            else if ( StringUtils.equals( SUBSCRIPTION_CATEGORY, strSubscriptionKey ) )
            {
                return StringUtils.EMPTY; //TODO : implement me !
            }
            else if ( StringUtils.equals( SUBSCRIPTION_FILTER, strSubscriptionKey ) )
            {
                AnnounceSearchFilter filter = AnnounceSearchFilterHome.findByPrimaryKey( Integer
                        .parseInt( strIdSubscribedResource ) );
                Map<String, Object> model = new HashMap<String, Object>( );
                model.put( MARK_FILTER, filter );
                if ( filter.getIdCategory( ) > 0 )
                {
                    model.put( MARK_CATEGORY, CategoryHome.findByPrimaryKey( filter.getIdCategory( ) ) );
                }
                HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_FILTER_SUBSCRIPTION_DESCRIPTION,
                        locale, model );
                return template.getHtml( );
            }
        }

        return StringUtils.EMPTY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSubscriptionRemovable( LuteceUser user, String strSubscriptionKey, String strIdSubscribedResource )
    {
        // Subscriptions to user and filters are always removable
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUrlModifySubscription( LuteceUser user, String strSubscriptionKey, String strIdSubscribedResource )
    {
        if ( StringUtils.equals( SUBSCRIPTION_FILTER, strSubscriptionKey ) )
        {
            int nIdFilter = ( ( strIdSubscribedResource != null ) && StringUtils.isNumeric( strIdSubscribedResource ) ) ? Integer
                    .parseInt( strIdSubscribedResource ) : 0;
            if ( nIdFilter > 0 )
            {
                return AnnounceApp.getUrlSearchAnnounce( LocalVariables.getRequest( ), nIdFilter );
            }
        }
        // subscriptions to users or categories can not be modified
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifySubscriptionRemoval( Subscription subscription )
    {
        if ( StringUtils.equals( subscription.getSubscriptionKey( ), SUBSCRIPTION_FILTER )
                && StringUtils.isNotEmpty( subscription.getIdSubscribedResource( ) )
                && StringUtils.isNumeric( subscription.getIdSubscribedResource( ) ) )
        {
            int nIdFilter = Integer.parseInt( subscription.getIdSubscribedResource( ) );
            AnnounceSearchFilterHome.delete( nIdFilter );
        }
        // We do nothing for users and category subscriptions
    }

    /**
     * Create a subscription to a user
     * @param user The user that subscribe to another one
     * @param strUserName The name of the user to subscribe to
     */
    public void createSubscriptionToUser( LuteceUser user, String strUserName )
    {
        createSubscription( user, strUserName, SUBSCRIPTION_USER );
    }

    /**
     * Create a subscription to a filter
     * @param user The user that subscribe to the filter
     * @param nIdFilter The id of the filter to subscribe to
     */
    public void createSubscriptionToFilter( LuteceUser user, int nIdFilter )
    {
        createSubscription( user, Integer.toString( nIdFilter ), SUBSCRIPTION_FILTER );
    }

    /**
     * Create a subscription to a category
     * @param user The user that subscribe to the category
     * @param nIdCategory The id of the category to subscribe to
     */
    public void createSubscriptionToCategory( LuteceUser user, int nIdCategory )
    {
        createSubscription( user, Integer.toString( nIdCategory ), SUBSCRIPTION_CATEGORY );
    }

    /**
     * Remove a subscription to a user
     * @param user The user that subscribe to another one
     * @param strUserName The name of the user to subscribe to
     */
    public void removeSubscriptionToUser( LuteceUser user, String strUserName )
    {
        removeSubscription( user, strUserName, SUBSCRIPTION_USER );
    }

    /**
     * Remove a subscription to a filter
     * @param user The user that subscribe to the filter
     * @param nIdFilter The id of the filter to subscribe to
     */
    public void removeSubscriptionToFilter( LuteceUser user, int nIdFilter )
    {
        removeSubscription( user, Integer.toString( nIdFilter ), SUBSCRIPTION_FILTER );
    }

    /**
     * Remove a subscription to a category
     * @param user The user that subscribe to the category
     * @param nIdCategory The id of the category to subscribe to
     */
    public void removeSubscriptionToCategory( LuteceUser user, int nIdCategory )
    {
        removeSubscription( user, Integer.toString( nIdCategory ), SUBSCRIPTION_CATEGORY );
    }

    /**
     * Do create a subscription to a user, a filter or a category
     * @param user The user to subscribe to
     * @param strIdResource the id of the resource to subscribe to
     * @param strSubscriptionKey The subscription key
     */
    private void createSubscription( LuteceUser user, String strIdResource, String strSubscriptionKey )
    {
        Subscription subscription = new Subscription( );
        subscription.setIdSubscribedResource( strIdResource );
        subscription.setSubscriptionKey( strSubscriptionKey );
        subscription.setSubscriptionProvider( getProviderName( ) );
        subscription.setUserId( user.getName( ) );

        SubscriptionService.getInstance( ).createSubscription( subscription, user );
    }

    private void removeSubscription( LuteceUser user, String strIdResource, String strSubscriptionKey )
    {
        SubscriptionFilter filter = new SubscriptionFilter( user.getName( ), getProviderName( ), strSubscriptionKey,
                strIdResource );
        List<Subscription> listSubscriptions = SubscriptionService.getInstance( ).findByFilter( filter );
        for ( Subscription subscription : listSubscriptions )
        {
            SubscriptionService.getInstance( ).removeSubscription( subscription, false );
        }
    }

}
